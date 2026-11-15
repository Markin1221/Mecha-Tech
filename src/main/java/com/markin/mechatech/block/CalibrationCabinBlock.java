package com.markin.mechatech.block;

import com.markin.mechatech.block.entity.CalibrationCabinBlockEntity;
import com.markin.mechatech.block.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Cabine de Calibração: cápsula redonda de vidro, 2 blocos de altura.
 * A porta (DOOR = 16 direções) gira pro lado do jogador e desliza aberta (OPEN = 0..3, animação).
 * Dá pra entrar por qualquer lado: só o piso e o teto têm colisão.
 */
public class CalibrationCabinBlock extends BaseEntityBlock {
	public static final MapCodec<CalibrationCabinBlock> CODEC = simpleCodec(CalibrationCabinBlock::new);
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	public static final IntegerProperty DOOR = IntegerProperty.create("door", 0, 15);
	public static final IntegerProperty OPEN = IntegerProperty.create("open", 0, 3);

	private static final VoxelShape OUTLINE = Block.box(1, 0, 1, 15, 16, 15);
	private static final VoxelShape FLOOR = Block.box(1, 0, 1, 15, 1, 15);
	private static final VoxelShape ROOF = Block.box(1, 14, 1, 15, 16, 15);

	public CalibrationCabinBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(HALF, DoubleBlockHalf.LOWER).setValue(DOOR, 0).setValue(OPEN, 0));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF, DOOR, OPEN);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return OUTLINE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? FLOOR : ROOF;
	}

	/** Direção (0..15, sentido horário a partir do norte) do centro da cabine até um ponto. */
	public static int directionTo(BlockPos lower, double x, double z) {
		double dx = x - (lower.getX() + 0.5), dz = z - (lower.getZ() + 0.5);
		double deg = Math.toDegrees(Math.atan2(dx, -dz));
		return Mth.positiveModulo(Math.round((float) (deg / 22.5)), 16);
	}

	/** Direção pra onde o jogador está olhando (0..15). */
	public static int directionOfLook(float yRot) {
		// yRot 0 = sul; nosso 0 = norte, sentido horário
		return Mth.positiveModulo(Math.round((yRot + 180f) / 22.5f), 16);
	}

	// ===== Colocar / quebrar as duas metades =====
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		if (pos.getY() < level.getMaxY() && level.getBlockState(pos.above()).canBeReplaced(context)) {
			int door = context.getPlayer() == null ? 0
				: directionTo(pos, context.getPlayer().getX(), context.getPlayer().getZ());
			return this.defaultBlockState().setValue(DOOR, door);
		}
		return null;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
									 Direction dir, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		DoubleBlockHalf half = state.getValue(HALF);
		boolean towardsOther = dir.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (dir == Direction.UP);
		if (towardsOther && !(neighborState.is(this) && neighborState.getValue(HALF) != half)) {
			return Blocks.AIR.defaultBlockState();
		}
		return state;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		if (state.getValue(HALF) == DoubleBlockHalf.LOWER) return true;
		BlockState below = level.getBlockState(pos.below());
		return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(DOOR, (state.getValue(DOOR) + rotation.ordinal() * 4) % 16);
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state;
	}

	// ===== Uso =====
	public static BlockPos lowerPos(BlockState state, BlockPos pos) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
	}

	/** O jogador está dentro da cabine (perto do centro). */
	public static boolean isInside(Player player, BlockPos lower) {
		double dx = player.getX() - (lower.getX() + 0.5), dz = player.getZ() - (lower.getZ() + 0.5);
		double dy = player.getY() - lower.getY();
		return dx * dx + dz * dz < 0.42 * 0.42 && dy > -0.2 && dy < 1.2;
	}

	public static AABB area(BlockPos lower) {
		return new AABB(lower).inflate(2.5, 0, 2.5).expandTowards(0, 1, 0);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		BlockPos lower = lowerPos(state, pos);
		if (!isInside(player, lower)) {
			if (!level.isClientSide()) {
				player.sendOverlayMessage(Component.translatable("message.mechatech.enter_cabin"));
			}
			return InteractionResult.SUCCESS;
		}
		if (!level.isClientSide() && level.getBlockEntity(lower) instanceof MenuProvider provider) {
			player.openMenu(provider);
		}
		return InteractionResult.SUCCESS;
	}

	/** Dentro da cabine, clicar com um corante pinta a exo-armadura. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
										  net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
		net.minecraft.world.item.DyeColor dye = stack.get(net.minecraft.core.component.DataComponents.DYE);
		if (dye == null || !isInside(player, lowerPos(state, pos))) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!level.isClientSide()) {
			if (com.markin.mechatech.menu.CalibrationCabinMenu.applyPaint(player, dye.getTextureDiffuseColor() & 0xFFFFFF)) {
				level.playSound(null, pos, net.minecraft.sounds.SoundEvents.DYE_USE, net.minecraft.sounds.SoundSource.PLAYERS, 1f, 1f);
				player.sendOverlayMessage(Component.translatable("message.mechatech.painted",
					Component.translatable("color.minecraft." + dye.getName())));
			}
		}
		return InteractionResult.SUCCESS;
	}

	// ===== Bloco com entidade (só na metade de baixo) =====
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new CalibrationCabinBlockEntity(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() || state.getValue(HALF) != DoubleBlockHalf.LOWER ? null
			: createTickerHelper(type, ModBlockEntities.CALIBRATION_CABIN, CalibrationCabinBlockEntity::tick);
	}
}
