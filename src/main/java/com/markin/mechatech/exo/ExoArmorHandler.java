package com.markin.mechatech.exo;

import com.markin.mechatech.MechaTech;
import com.markin.mechatech.block.ModBlocks;
import com.markin.mechatech.component.ModComponents;
import com.markin.mechatech.items.ModArmorMaterials;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Uma vez por segundo: para cada peça da exo-armadura vestida, gasta a energia
 * dos módulos dela e aplica os efeitos. Peça sem energia = sem poderes e sem proteção.
 */
public final class ExoArmorHandler {
	private static final Identifier STEP_ID = MechaTech.id("exo_step_assist");
	private static final Identifier SAFE_FALL_ID = MechaTech.id("exo_fall_damping");
	private static final Identifier FALL_MULT_ID = MechaTech.id("exo_fall_damping_mult");
	private static final Identifier STRENGTH_ID = MechaTech.id("exo_strength");
	private static final Identifier SHIELD_ID = MechaTech.id("exo_shield");
	private static final Identifier SHIELD_T_ID = MechaTech.id("exo_shield_toughness");
	private static final Identifier SPEED_ID = MechaTech.id("exo_speed");
	private static final Identifier JUMP_ID = MechaTech.id("exo_jump");
	private static final Identifier JUMP_FALL_ID = MechaTech.id("exo_jump_fall");
	private static final Identifier BREATH_ID = MechaTech.id("exo_breath");
	private static final Identifier NO_ARMOR_ID = MechaTech.id("exo_unpowered_armor");
	private static final Identifier NO_TOUGH_ID = MechaTech.id("exo_unpowered_toughness");
	private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	private static final ArmorType[] TYPES = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};
	/** EM gasta por ponto de dano recebido (dividido entre as peças). */
	private static final int ENERGY_PER_DAMAGE = 60;

	private ExoArmorHandler() {}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(ExoArmorHandler::onTick);
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) -> {
			if (entity instanceof ServerPlayer player && taken > 0 && !player.isCreative()) {
				drainOnDamage(player, taken);
			}
		});
	}

	private static void onTick(MinecraftServer server) {
		if (server.getTickCount() % 20 != 0) return;
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			update(player);
		}
	}

	/** Dentro da cabine os módulos desligam (pra recarregar sem gastar). */
	public static boolean isInCabin(ServerPlayer player) {
		BlockPos pos = player.blockPosition();
		return player.level().getBlockState(pos).is(ModBlocks.CALIBRATION_CABIN);
	}

	private static void update(ServerPlayer player) {
		boolean inCabin = isInCabin(player);
		boolean step = false, fall = false, strength = false, shield = false, speed = false, jump = false, breath = false;
		double lostArmor = 0, lostTough = 0;

		for (int i = 0; i < 4; i++) {
			ItemStack piece = player.getItemBySlot(SLOTS[i]);
			if (!ExoArmorItem.isExo(piece)) continue;

			int energy = ExoArmorItem.getEnergy(piece);
			if (energy <= 0) {
				// Sem energia: perde a proteção dessa peça
				lostArmor += ModArmorMaterials.EXO.defense().getOrDefault(TYPES[i], 0);
				lostTough += ModArmorMaterials.EXO.toughness();
				continue;
			}
			if (inCabin) continue;

			int cost = 0;
			for (ItemStack s : piece.getOrDefault(ModComponents.EXO_MODULES, ItemContainerContents.EMPTY)
					.nonEmptyItemCopyStream().toList()) {
				if (!(s.getItem() instanceof ExoModuleItem m)) continue;
				if (cost + m.module.costPerSecond > energy) break;
				boolean used = switch (m.module) {
					case NIGHT_VISION -> {
						if (isDark(player)) { effect(player, MobEffects.NIGHT_VISION, 0, 260); yield true; }
						yield false;
					}
					case WATER_BREATHING -> { breath = true; yield player.isEyeInFluid(FluidTags.WATER); }
					case STRENGTH -> { strength = true; yield true; }
					case RESISTANCE -> { shield = true; yield true; }
					case SPEED -> { speed = true; yield player.isSprinting(); }
					case JUMP_BOOST -> { jump = true; yield true; }
					case STEP_ASSIST -> { step = true; yield true; }
					case FALL_DAMPING -> {
						fall = true;
						yield !player.onGround();
					}
				};
				if (used) cost += m.module.costPerSecond;
			}
			if (cost > 0 && !player.isCreative()) {
				ExoArmorItem.setEnergy(piece, energy - cost);
			}
		}

		modifier(player, Attributes.STEP_HEIGHT, STEP_ID, step ? 0.5 : 0);
		modifier(player, Attributes.SAFE_FALL_DISTANCE, SAFE_FALL_ID, fall ? 8 : 0);
		modifier(player, Attributes.FALL_DAMAGE_MULTIPLIER, FALL_MULT_ID, fall ? -0.5 : 0);
		// Módulos via atributos (não aparecem como "efeito" na tela)
		modifier(player, Attributes.ATTACK_DAMAGE, STRENGTH_ID, strength ? 3 : 0);
		modifier(player, Attributes.ARMOR, SHIELD_ID, shield ? 4 : 0);
		modifier(player, Attributes.ARMOR_TOUGHNESS, SHIELD_T_ID, shield ? 2 : 0);
		modifier(player, Attributes.MOVEMENT_SPEED, SPEED_ID, speed && player.isSprinting() ? 0.035 : 0);
		modifier(player, Attributes.JUMP_STRENGTH, JUMP_ID, jump ? 0.15 : 0);
		modifier(player, Attributes.SAFE_FALL_DISTANCE, JUMP_FALL_ID, jump ? 2 : 0);
		modifier(player, Attributes.OXYGEN_BONUS, BREATH_ID, breath ? 1000 : 0);
		modifier(player, Attributes.ARMOR, NO_ARMOR_ID, -lostArmor);
		modifier(player, Attributes.ARMOR_TOUGHNESS, NO_TOUGH_ID, -lostTough);
	}

	private static boolean isDark(ServerPlayer player) {
		return player.level().getMaxLocalRawBrightness(BlockPos.containing(player.getEyePosition())) < 8;
	}

	/** Levar dano gasta energia das peças (em vez de gastar durabilidade). */
	private static void drainOnDamage(ServerPlayer player, float damage) {
		int pieces = 0;
		for (EquipmentSlot slot : SLOTS) if (ExoArmorItem.hasPower(player.getItemBySlot(slot))) pieces++;
		if (pieces == 0) return;
		int each = Math.max(1, Math.round(damage * ENERGY_PER_DAMAGE / pieces));
		for (EquipmentSlot slot : SLOTS) {
			ItemStack piece = player.getItemBySlot(slot);
			if (ExoArmorItem.hasPower(piece)) ExoArmorItem.setEnergy(piece, ExoArmorItem.getEnergy(piece) - each);
		}
	}

	private static void effect(ServerPlayer player, Holder<MobEffect> effect, int amplifier, int duration) {
		player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, false));
	}

	private static void modifier(ServerPlayer player, Holder<Attribute> attribute, Identifier id, double value) {
		AttributeInstance inst = player.getAttribute(attribute);
		if (inst == null) return;
		if (value == 0) {
			inst.removeModifier(id);
		} else {
			inst.addOrUpdateTransientModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE));
		}
	}
}
