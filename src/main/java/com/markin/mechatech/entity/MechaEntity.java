package com.markin.mechatech.entity;

import java.util.function.Predicate;

import com.markin.mechatech.block.entity.PartForgeBlockEntity;
import com.markin.mechatech.items.MechaItem;
import com.markin.mechatech.items.ModItems;
import com.markin.mechatech.mecha.MechaData;
import com.markin.mechatech.mecha.MechaPartData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Mecha montado: entidade pilotável. As peças definem tamanho, vida, blindagem, velocidade, pulo e consumo.
 * Clique direito = entrar. Agachado com chave = empacotar de volta em item.
 */
public class MechaEntity extends LivingEntity {
	private static final EntityDataAccessor<String> PARTS = SynchedEntityData.defineId(MechaEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Integer> ENERGY = SynchedEntityData.defineId(MechaEntity.class, EntityDataSerializers.INT);

	/** Cliente diz se a tecla de pulo está apertada (registrado no MechaTechClient). */
	public static Predicate<Player> JUMP_KEY = Player::isJumping;

	private MechaData cached;
	private String cachedKey = null;

	public MechaEntity(EntityType<? extends MechaEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes()
			.add(Attributes.MAX_HEALTH, 60)
			.add(Attributes.MOVEMENT_SPEED, 0.15)
			.add(Attributes.STEP_HEIGHT, 1.1)
			.add(Attributes.JUMP_STRENGTH, 0.6)
			.add(Attributes.SAFE_FALL_DISTANCE, 24)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(PARTS, "");
		builder.define(ENERGY, 0);
	}

	// ===== Dados =====
	public MechaData data() {
		if (entityData == null) return MechaData.EMPTY;
		String key = entityData.get(PARTS);
		if (cached == null || !key.equals(cachedKey)) {
			cached = MechaData.decode(key, 0);
			cachedKey = key;
		}
		return cached.withEnergy(energy());
	}

	public int energy() {
		return entityData == null ? 0 : entityData.get(ENERGY);
	}

	public void setEnergy(int e) {
		entityData.set(ENERGY, Math.max(0, Math.min(e, data().maxEnergy())));
	}

	public void setData(MechaData d) {
		entityData.set(PARTS, d.encode());
		cached = null;
		entityData.set(ENERGY, d.energy());
		applyStats(true);
	}

	private void applyStats(boolean heal) {
		MechaData d = data();
		getAttribute(Attributes.MAX_HEALTH).setBaseValue(d.maxHealth());
		getAttribute(Attributes.ARMOR).setBaseValue(Math.min(30, d.armor() / 2.0));
		getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(d.overweight() ? 0.05 : 0.07 + d.speed() * 0.014);
		getAttribute(Attributes.STEP_HEIGHT).setBaseValue(Math.max(1.1, d.height() * 0.4));
		getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(d.jump());
		if (heal) setHealth(getMaxHealth());
		refreshDimensions();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (PARTS.equals(key)) {
			cached = null;
			refreshDimensions();
		}
	}

	@Override
	protected EntityDimensions getDefaultDimensions(Pose pose) {
		if (entityData == null) return super.getDefaultDimensions(pose);
		float h = data().height();
		return EntityDimensions.scalable(h * 0.48f, h).withEyeHeight(h * 0.86f);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.store("Mecha", MechaData.CODEC, data());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		float hp = getHealth();
		input.read("Mecha", MechaData.CODEC).ifPresent(d -> {
			entityData.set(PARTS, d.encode());
			cached = null;
			entityData.set(ENERGY, d.energy());
			applyStats(false);
		});
		setHealth(hp);
	}

	public ItemStack toItem() {
		ItemStack stack = new ItemStack(ModItems.MECHA);
		stack.set(com.markin.mechatech.component.ModComponents.MECHA_DATA, data());
		return stack;
	}

	// ===== Interação =====
	@Override
	public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
		ItemStack held = player.getItemInHand(hand);
		if (player.isShiftKeyDown() && held.is(ModItems.WRENCH)) {
			if (level() instanceof ServerLevel sl) {
				ejectPassengers();
				ItemStack item = toItem();
				if (!player.getInventory().add(item)) spawnAtLocation(sl, item);
				sl.playSound(null, blockPosition(), SoundEvents.PISTON_CONTRACT, SoundSource.NEUTRAL, 1f, 0.7f);
				discard();
			}
			return InteractionResult.SUCCESS;
		}
		if (!isVehicle() && !player.isShiftKeyDown()) {
			if (!level().isClientSide()) {
				player.setYRot(getYRot());
				player.startRiding(this);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public LivingEntity getControllingPassenger() {
		return getFirstPassenger() instanceof Player p ? p : null;
	}

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dims, float scale) {
		return new Vec3(0, dims.height() * 0.5, 0);
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		if (passenger instanceof LivingEntity le && !level().isClientSide()) le.removeEffect(MobEffects.INVISIBILITY);
	}

	// ===== Pilotagem =====
	@Override
	protected void tickRidden(Player player, Vec3 input) {
		super.tickRidden(player, input);
		float yaw = player.getYRot();
		setRot(yaw, 0);
		this.yRotO = this.yBodyRot = this.yHeadRot = yaw;
		if (energy() <= 0) return;
		Vec3 dm = getDeltaMovement();
		if (JUMP_KEY.test(player)) {
			if (onGround()) {
				setDeltaMovement(dm.x, getAttributeValue(Attributes.JUMP_STRENGTH), dm.z);
				this.needsSync = true;
			} else if (data().has("jet_backpack")) {
				setDeltaMovement(dm.x, Math.min(dm.y + 0.09, 0.38), dm.z);
				if (level().isClientSide() && tickCount % 2 == 0) {
					level().addParticle(ParticleTypes.FLAME, getX(), getY() + data().height() * 0.45, getZ(), 0, -0.25, 0);
				}
			}
		}
	}

	@Override
	protected Vec3 getRiddenInput(Player player, Vec3 travel) {
		if (energy() <= 0) return Vec3.ZERO;
		float fwd = player.zza;
		if (fwd < 0) fwd *= 0.45f;
		return new Vec3(player.xxa * 0.6f, 0, fwd);
	}

	@Override
	protected float getRiddenSpeed(Player player) {
		return (float) getAttributeValue(Attributes.MOVEMENT_SPEED);
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel sl)) return;
		if (getFirstPassenger() instanceof ServerPlayer sp) {
			Input in = sp.getLastClientInput();
			boolean moving = in.forward() || in.backward() || in.left() || in.right();
			int use = 0;
			if (moving) use += data().usage();
			if (in.jump() && !onGround() && data().has("jet_backpack")) use += data().usage() * 3;
			if (use > 0 && energy() > 0) setEnergy(energy() - use);
			// Piloto fica escondido dentro do mecha
			if (tickCount % 40 == 0) sp.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, true, false, false));
			if (tickCount % 10 == 0) {
				MechaData d = data();
				int pct = d.maxEnergy() == 0 ? 0 : Math.round(100f * energy() / d.maxEnergy());
				sp.sendOverlayMessage(Component.translatable("message.mechatech.mecha_hud",
					Math.round(getHealth()), Math.round(getMaxHealth()), pct)
					.withColor(pct > 20 ? 0x7DF9E8 : 0xFF7060));
			}
		}
	}

	// ===== Dano / morte =====
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.getEntity() != null && hasPassenger(source.getEntity())) return false;
		return super.hurtServer(level, source, amount);
	}

	@Override
	public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
		if (distance > 3 && level() instanceof ServerLevel sl) {
			sl.playSound(null, blockPosition(), SoundEvents.IRON_GOLEM_STEP, SoundSource.NEUTRAL, 1.5f, 0.5f);
		}
		return super.causeFallDamage(distance, multiplier * 0.25f, source);
	}

	@Override
	protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
		for (MechaPartData p : data().parts()) {
			if (MechaData.present(p)) spawnAtLocation(level, PartForgeBlockEntity.createPart(p.def(), p.scale()));
		}
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		playSound(SoundEvents.IRON_GOLEM_STEP, 0.8f, 0.6f);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.IRON_GOLEM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.IRON_GOLEM_DEATH;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isPickable() {
		return isAlive();
	}

	@Override
	public HumanoidArm getMainArm() {
		return HumanoidArm.RIGHT;
	}
}
