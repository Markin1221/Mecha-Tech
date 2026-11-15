package com.markin.mechatech.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.markin.mechatech.block.entity.PartForgeBlockEntity;
import com.markin.mechatech.items.ModItems;
import com.markin.mechatech.mecha.MechaData;
import com.markin.mechatech.mecha.MechaLayout;
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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Mecha montado: entidade pilotável. As peças definem tamanho, vida, blindagem, velocidade, pulo, consumo e habilidades.
 * Clique direito = entrar. Agachado = compartimento (mochila de carga). Agachado com chave = empacotar.
 */
public class MechaEntity extends LivingEntity {
	private static final EntityDataAccessor<String> PARTS = SynchedEntityData.defineId(MechaEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Integer> ENERGY = SynchedEntityData.defineId(MechaEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> COOLDOWN = SynchedEntityData.defineId(MechaEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> COOLDOWN_MAX = SynchedEntityData.defineId(MechaEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> SHIELD = SynchedEntityData.defineId(MechaEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(MechaEntity.class, EntityDataSerializers.INT);

	/** Cliente diz se a tecla de pulo está apertada. */
	public static Predicate<Player> JUMP_KEY = Player::isJumping;

	private MechaData cached;
	private String cachedKey = null;
	private MechaLayout layout;
	public final SimpleContainer storage = new SimpleContainer(27);

	// ===== Animação (só no cliente) =====
	public float animPos, animPosO, animSpeed, animSpeedO, air, airO, land, landO, actionAnim, actionAnimO;
	private boolean wasOnGround = true;
	private int fallTicks, lastAction;

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
		builder.define(COOLDOWN, 0);
		builder.define(COOLDOWN_MAX, 1);
		builder.define(SHIELD, 0);
		builder.define(ACTION, 0);
	}

	// ===== Dados =====
	public MechaData data() {
		if (entityData == null) return MechaData.EMPTY;
		String key = entityData.get(PARTS);
		if (cached == null || !key.equals(cachedKey)) {
			cached = MechaData.decode(key, 0);
			cachedKey = key;
			layout = new MechaLayout(cached);
		}
		return cached.withEnergy(energy());
	}

	public MechaLayout layout() {
		data();
		return layout;
	}

	public int energy() { return entityData == null ? 0 : entityData.get(ENERGY); }
	public int cooldown() { return entityData.get(COOLDOWN); }
	public int cooldownMax() { return Math.max(1, entityData.get(COOLDOWN_MAX)); }
	public int shield() { return entityData.get(SHIELD); }

	public void setEnergy(int e) {
		entityData.set(ENERGY, Math.max(0, Math.min(e, data().maxEnergy())));
	}

	public boolean useEnergy(int amount) {
		if (getFirstPassenger() instanceof Player p && p.isCreative()) return true;
		if (energy() < amount) return false;
		setEnergy(energy() - amount);
		return true;
	}

	public void setCooldown(int ticks) {
		entityData.set(COOLDOWN, ticks);
		entityData.set(COOLDOWN_MAX, Math.max(1, ticks));
	}

	public void setShield(int ticks) { entityData.set(SHIELD, ticks); }

	/** Avisa os clientes pra tocar a animação de habilidade. */
	public void playAction() { entityData.set(ACTION, entityData.get(ACTION) + 1); }

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
		return EntityDimensions.scalable(h * 0.48f, h).withEyeHeight(layout().eyeY());
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.store("Mecha", MechaData.CODEC, data());
		List<ItemStack> list = new ArrayList<>();
		for (int i = 0; i < storage.getContainerSize(); i++) list.add(storage.getItem(i));
		output.store("Storage", ItemStack.OPTIONAL_CODEC.listOf(), list);
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
		input.read("Storage", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> {
			for (int i = 0; i < list.size() && i < storage.getContainerSize(); i++) storage.setItem(i, list.get(i));
		});
		setHealth(hp);
	}

	public ItemStack toItem() {
		ItemStack stack = new ItemStack(ModItems.MECHA);
		stack.set(com.markin.mechatech.component.ModComponents.MECHA_DATA, data());
		return stack;
	}

	private void dropStorage(ServerLevel level) {
		for (int i = 0; i < storage.getContainerSize(); i++) {
			ItemStack s = storage.removeItemNoUpdate(i);
			if (!s.isEmpty()) spawnAtLocation(level, s);
		}
	}

	// ===== Interação =====
	@Override
	public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
		ItemStack held = player.getItemInHand(hand);
		if (player.isShiftKeyDown() && held.is(ModItems.WRENCH)) {
			if (level() instanceof ServerLevel sl) {
				ejectPassengers();
				dropStorage(sl);
				ItemStack item = toItem();
				if (!player.getInventory().add(item)) spawnAtLocation(sl, item);
				sl.playSound(null, blockPosition(), SoundEvents.PISTON_CONTRACT, SoundSource.NEUTRAL, 1f, 0.7f);
				discard();
			}
			return InteractionResult.SUCCESS;
		}
		if (player.isShiftKeyDown()) {
			if (data().has("storage_backpack")) {
				if (!level().isClientSide()) {
					player.openMenu(new SimpleMenuProvider((id, inv, p) -> ChestMenu.threeRows(id, inv, storage),
						Component.translatable("container.mechatech.mecha_storage")));
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}
		if (!isVehicle()) {
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

	/** Piloto fica com os olhos dentro da cabeça do mecha. */
	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dims, float scale) {
		return new Vec3(0, Math.max(0, layout().eyeY() - passenger.getEyeHeight(Pose.STANDING)), 0);
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		if (passenger instanceof ServerPlayer sp) MechaAbilities.clearPilot(sp);
	}

	// ===== Pilotagem =====
	@Override
	protected void tickRidden(Player player, Vec3 input) {
		super.tickRidden(player, input);
		float yaw = player.getYRot();
		setYRot(yaw);
		this.yRotO = getYRot();
		// O corpo vira aos poucos (mecha pesado), a cabeça segue a câmera
		this.yBodyRot = Mth.approachDegrees(this.yBodyRot, yaw, 9f);
		this.yHeadRot = yaw;
		if (energy() <= 0) return;
		Vec3 dm = getDeltaMovement();
		if (JUMP_KEY.test(player)) {
			if (onGround()) {
				setDeltaMovement(dm.x, getAttributeValue(Attributes.JUMP_STRENGTH), dm.z);
				this.needsSync = true;
			} else if (data().has("jet_backpack")) {
				setDeltaMovement(dm.x, Math.min(dm.y + 0.09, 0.38), dm.z);
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
		if (level().isClientSide()) {
			clientAnim();
			return;
		}
		ServerLevel sl = (ServerLevel) level();
		if (cooldown() > 0) entityData.set(COOLDOWN, cooldown() - 1);
		if (shield() > 0) setShield(shield() - 1);
		if (getFirstPassenger() instanceof ServerPlayer sp) {
			Input in = sp.getLastClientInput();
			boolean moving = in.forward() || in.backward() || in.left() || in.right();
			int use = 0;
			if (moving) use += data().usage();
			if (in.jump() && !onGround() && data().has("jet_backpack")) use += data().usage() * 3;
			if (use > 0 && energy() > 0 && !sp.isCreative()) setEnergy(energy() - use);
			if (tickCount % 10 == 0) MechaAbilities.applyPilot(sp, this);
			if (tickCount % 20 == 0) MechaAbilities.headScan(sp, this, sl);
		}
	}

	private void clientAnim() {
		animPosO = animPos; animSpeedO = animSpeed; airO = air; landO = land; actionAnimO = actionAnim;
		double dx = getX() - xo, dz = getZ() - zo;
		float dist = (float) Math.sqrt(dx * dx + dz * dz);
		float h = Math.max(1.5f, data().height());
		animSpeed += (Math.min(1f, dist * 9f / h) - animSpeed) * 0.3f;
		animPos += dist * (Mth.TWO_PI / (h * 0.95f));
		boolean ground = onGround();
		air += ((ground ? 0f : 1f) - air) * 0.25f;
		fallTicks = ground ? 0 : fallTicks + 1;
		if (ground && !wasOnGround) land = 1f;
		wasOnGround = ground;
		land *= 0.82f;
		int act = entityData.get(ACTION);
		if (act != lastAction) { lastAction = act; actionAnim = 1f; }
		actionAnim = Math.max(0, actionAnim - 0.08f);
		if (!ground && getFirstPassenger() instanceof Player p && JUMP_KEY.test(p) && data().has("jet_backpack") && energy() > 0) {
			MechaLayout l = layout();
			float r = Mth.DEG_TO_RAD * yBodyRot;
			double bx = getX() + Mth.sin(r) * l.packZ, bz = getZ() - Mth.cos(r) * l.packZ;
			for (int s = -1; s <= 1; s += 2) {
				double ox = Mth.cos(r) * s * l.pack * 0.3, oz = Mth.sin(r) * s * l.pack * 0.3;
				level().addParticle(ParticleTypes.FLAME, bx + ox, getY() + l.torsoBottom, bz + oz, 0, -0.3, 0);
				level().addParticle(ParticleTypes.SMOKE, bx + ox, getY() + l.torsoBottom - 0.2, bz + oz, 0, -0.1, 0);
			}
		}
	}

	// ===== Dano / morte =====
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.getEntity() != null && hasPassenger(source.getEntity())) return false;
		if (shield() > 0) {
			amount *= 0.25f;
			level.playSound(null, blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.NEUTRAL, 1f, 0.7f);
		}
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
		dropStorage(level);
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
