package com.markin.mechatech.entity;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import com.markin.mechatech.MechaTech;
import com.markin.mechatech.mecha.MechaData;
import com.markin.mechatech.mecha.MechaLayout;
import com.markin.mechatech.network.MechaActionPayload;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Habilidades das peças do mecha.
 * Passivas: alcance, força, broca (minera 3x3), serra (derruba a árvore), cabeça (visão noturna / térmica / radar).
 * Ativas (botão direito): canhão, garra, escudo, punho (impacto no chão).
 */
public final class MechaAbilities {
	private static final Identifier REACH = MechaTech.id("mecha_reach"), REACH_B = MechaTech.id("mecha_reach_block"),
		DAMAGE = MechaTech.id("mecha_damage"), KNOCK = MechaTech.id("mecha_knockback"), MINE = MechaTech.id("mecha_mining");
	private static boolean breaking;

	private MechaAbilities() { }

	public static void register() {
		PayloadTypeRegistry.serverboundPlay().register(MechaActionPayload.TYPE, MechaActionPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(MechaActionPayload.TYPE, (payload, ctx) ->
			ctx.server().execute(() -> {
				if (ctx.player().getVehicle() instanceof MechaEntity m) useActive(ctx.player(), m);
			}));
		PlayerBlockBreakEvents.BEFORE.register(MechaAbilities::beforeBreak);
	}

	// ===== Passivas no piloto =====
	public static void applyPilot(ServerPlayer p, MechaEntity m) {
		MechaData d = m.data();
		double reach = d.height() * 0.8;
		boolean fist = d.has("fist_arm");
		boolean drill = d.has("drill_arm"), saw = d.has("saw_arm");
		mod(p, Attributes.ENTITY_INTERACTION_RANGE, REACH, reach, AttributeModifier.Operation.ADD_VALUE);
		mod(p, Attributes.BLOCK_INTERACTION_RANGE, REACH_B, reach, AttributeModifier.Operation.ADD_VALUE);
		mod(p, Attributes.ATTACK_DAMAGE, DAMAGE, d.power() * 0.5 + (fist ? 4 : 0), AttributeModifier.Operation.ADD_VALUE);
		mod(p, Attributes.ATTACK_KNOCKBACK, KNOCK, fist ? 1.5 : 0.5, AttributeModifier.Operation.ADD_VALUE);
		mod(p, Attributes.BLOCK_BREAK_SPEED, MINE, drill ? 14 : saw ? 9 : 1.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}

	public static void clearPilot(ServerPlayer p) {
		for (var e : new Object[][]{{Attributes.ENTITY_INTERACTION_RANGE, REACH}, {Attributes.BLOCK_INTERACTION_RANGE, REACH_B},
			{Attributes.ATTACK_DAMAGE, DAMAGE}, {Attributes.ATTACK_KNOCKBACK, KNOCK}, {Attributes.BLOCK_BREAK_SPEED, MINE}}) {
			@SuppressWarnings("unchecked") AttributeInstance inst = p.getAttribute((Holder<Attribute>) e[0]);
			if (inst != null) inst.removeModifier((Identifier) e[1]);
		}
	}

	private static void mod(ServerPlayer p, Holder<Attribute> attr, Identifier id, double v, AttributeModifier.Operation op) {
		AttributeInstance inst = p.getAttribute(attr);
		if (inst != null) inst.addOrUpdateTransientModifier(new AttributeModifier(id, v, op));
	}

	// ===== Cabeça (a cada 1 s) =====
	public static void headScan(ServerPlayer p, MechaEntity m, ServerLevel level) {
		MechaData d = m.data();
		if (d.has("sensor_head")) {
			p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, false));
		} else if (d.has("thermal_head") || d.has("radar_head")) {
			boolean radar = d.has("radar_head");
			double r = radar ? 48 : 24;
			if (!m.useEnergy(radar ? 30 : 20)) return;
			for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, m.getBoundingBox().inflate(r),
				e -> e != m && e != p && (!radar || e instanceof Enemy))) {
				e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, true, false, false));
			}
		}
	}

	// ===== Ativa (botão direito) =====
	private static String activeArm(MechaData d) {
		for (String v : new String[]{d.armLeft().variant(), d.armRight().variant()}) {
			if (v.equals("cannon_arm") || v.equals("claw_arm") || v.equals("shield_arm") || v.equals("fist_arm")) return v;
		}
		return "";
	}

	public static String activeName(MechaData d) {
		return activeArm(d);
	}

	private static void useActive(ServerPlayer p, MechaEntity m) {
		if (m.cooldown() > 0) return;
		ServerLevel level = (ServerLevel) m.level();
		MechaData d = m.data();
		MechaLayout l = m.layout();
		float s = d.scale() / 100f;
		Vec3 look = p.getLookAngle();
		switch (activeArm(d)) {
			case "cannon_arm" -> {
				if (!m.useEnergy(600)) return;
				Vec3 from = m.position().add(0, l.armY_L + l.armL * 0.1, 0).add(look.scale(m.getBbWidth() * 0.7 + 1.2));
				LargeFireball fb = new LargeFireball(level, p, look.scale(1.6), s >= 1.1f ? 2 : 1);
				fb.setPos(from);
				level.addFreshEntity(fb);
				level.playSound(null, m.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8f, 1.6f);
				m.setCooldown(30);
			}
			case "claw_arm" -> {
				if (!m.useEnergy(120)) return;
				Vec3 c = m.position().add(0, l.torsoBottom, 0);
				for (var e : level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, m.getBoundingBox().inflate(14),
					e -> e instanceof ItemEntity || e instanceof ExperienceOrb)) {
					e.setDeltaMovement(c.subtract(e.position()).normalize().scale(0.9));
				}
				for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, m.getBoundingBox().inflate(8), e -> e != m && e != p)) {
					e.setDeltaMovement(c.subtract(e.position()).normalize().scale(0.8).add(0, 0.25, 0));
					e.hurtMarked = true;
				}
				level.playSound(null, m.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.2f, 0.6f);
				m.setCooldown(25);
			}
			case "shield_arm" -> {
				if (!m.useEnergy(300)) return;
				m.setShield(80);
				level.playSound(null, m.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1f, 1.4f);
				m.setCooldown(160);
			}
			case "fist_arm" -> {
				if (!m.useEnergy(350)) return;
				double r = 3.5 * s;
				for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, m.getBoundingBox().inflate(r, 1, r), e -> e != m && e != p)) {
					e.hurtServer(level, level.damageSources().playerAttack(p), 6 + d.power() * 0.6f);
					Vec3 push = e.position().subtract(m.position()).normalize().scale(1.4);
					e.setDeltaMovement(push.x, 0.6, push.z);
					e.hurtMarked = true;
				}
				level.sendParticles(ParticleTypes.EXPLOSION, m.getX(), m.getY() + 0.2, m.getZ(), 6, r * 0.5, 0.1, r * 0.5, 0);
				level.playSound(null, m.blockPosition(), SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.PLAYERS, 1.2f, 0.8f);
				m.setCooldown(50);
			}
			default -> { return; }
		}
		m.playAction();
	}

	// ===== Broca 3x3 / serra derruba árvore =====
	private static boolean beforeBreak(Level level, Player player, BlockPos pos, BlockState state, BlockEntity be) {
		if (breaking || !(level instanceof ServerLevel sl) || !(player.getVehicle() instanceof MechaEntity m)) return true;
		MechaData d = m.data();
		boolean drill = d.has("drill_arm"), saw = d.has("saw_arm");
		if (saw && state.is(BlockTags.LOGS)) {
			fellTree(sl, player, m, pos, state);
			return false;
		}
		if (drill && (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL))) {
			boolean shovel = !state.is(BlockTags.MINEABLE_WITH_PICKAXE);
			ItemStack tool = new ItemStack(shovel ? Items.DIAMOND_SHOVEL : Items.DIAMOND_PICKAXE);
			Direction.Axis axis = lookAxis(player.getLookAngle());
			float hardness = state.getDestroySpeed(level, pos);
			breaking = true;
			try {
				for (int a = -1; a <= 1; a++) {
					for (int b = -1; b <= 1; b++) {
						BlockPos q = switch (axis) {
							case X -> pos.offset(0, a, b);
							case Y -> pos.offset(a, 0, b);
							default -> pos.offset(a, b, 0);
						};
						BlockState qs = level.getBlockState(q);
						float h = qs.getDestroySpeed(level, q);
						if (qs.isAir() || h < 0 || h > hardness + 3) continue;
						if (!(qs.is(BlockTags.MINEABLE_WITH_PICKAXE) || qs.is(BlockTags.MINEABLE_WITH_SHOVEL))) continue;
						if (!q.equals(pos) && (level.getBlockEntity(q) != null || !m.useEnergy(25))) continue;
						breakWith(sl, player, q, qs, tool);
					}
				}
			} finally {
				breaking = false;
			}
			return false;
		}
		return true;
	}

	private static Direction.Axis lookAxis(Vec3 v) {
		double x = Math.abs(v.x), y = Math.abs(v.y), z = Math.abs(v.z);
		return y > x && y > z ? Direction.Axis.Y : x > z ? Direction.Axis.X : Direction.Axis.Z;
	}

	private static void breakWith(ServerLevel level, Player player, BlockPos pos, BlockState state, ItemStack tool) {
		BlockEntity be = level.getBlockEntity(pos);
		if (!player.isCreative()) Block.dropResources(state, level, pos, be, player, tool);
		level.destroyBlock(pos, false, player);
	}

	private static void fellTree(ServerLevel level, Player player, MechaEntity m, BlockPos start, BlockState state) {
		ItemStack axe = new ItemStack(Items.DIAMOND_AXE);
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		Set<BlockPos> seen = new HashSet<>();
		queue.add(start);
		seen.add(start);
		int count = 0;
		breaking = true;
		try {
			while (!queue.isEmpty() && count < 128) {
				BlockPos p = queue.poll();
				BlockState s = level.getBlockState(p);
				if (!s.is(BlockTags.LOGS)) continue;
				if (count > 0 && !m.useEnergy(15)) break;
				breakWith(level, player, p, s, axe);
				count++;
				for (int dx = -1; dx <= 1; dx++) for (int dy = 0; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
					BlockPos n = p.offset(dx, dy, dz);
					if (seen.add(n) && level.getBlockState(n).is(BlockTags.LOGS)) queue.add(n);
				}
			}
		} finally {
			breaking = false;
		}
		if (count > 1) level.playSound(null, start, SoundEvents.AXE_STRIP, SoundSource.PLAYERS, 1f, 0.6f);
	}
}
