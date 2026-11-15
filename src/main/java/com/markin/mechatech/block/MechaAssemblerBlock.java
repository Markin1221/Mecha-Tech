package com.markin.mechatech.block;

import com.markin.mechatech.block.entity.MechaAssemblerBlockEntity;
import com.markin.mechatech.block.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Montadora de Mecha: máquina NxNxN (3, 4 ou 5). Quanto maior, maior o mecha que cabe nela.
 * PART: x = p % N (pra direita), y = (p / N) % N (pra cima), z = p / N² (pra trás). Parte 0 guarda a entidade.
 */
public abstract class MechaAssemblerBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final IntegerProperty PART3 = IntegerProperty.create("part", 0, 26);
	public static final IntegerProperty PART4 = IntegerProperty.create("part", 0, 63);
	public static final IntegerProperty PART5 = IntegerProperty.create("part", 0, 124);

	protected MechaAssemblerBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(part(), 0));
	}

	public abstract int size();
	public abstract IntegerProperty part();

	/** Escala máxima das peças que cabe nessa montadora. */
	public static int maxScale(int size) {
		return size <= 3 ? 90 : size == 4 ? 110 : 130;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, part());
	}

	public static Direction right(Direction f) { return f.getClockWise(); }
	public static Direction back(Direction f) { return f.getOpposite(); }

	public BlockPos offset(int p, Direction f) {
		int n = size();
		return BlockPos.ZERO.relative(right(f), p % n).above((p / n) % n).relative(back(f), p / (n * n));
	}

	public BlockPos mainPos(BlockState state, BlockPos pos) {
		return pos.subtract(offset(state.getValue(part()), state.getValue(FACING)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction f = ctx.getHorizontalDirection().getOpposite();
		int n = size();
		BlockPos main = ctx.getClickedPos().relative(right(f), -(n / 2));
		Level level = ctx.getLevel();
		for (int p = 0; p < n * n * n; p++) {
			BlockPos q = main.offset(offset(p, f));
			if (q.getY() > level.getMaxY() || !level.getBlockState(q).canBeReplaced(ctx)) return null;
		}
		return this.defaultBlockState().setValue(FACING, f).setValue(part(), n / 2);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		BlockPos main = mainPos(state, pos);
		int n = size();
		for (int p = 0; p < n * n * n; p++) {
			BlockPos q = main.offset(offset(p, state.getValue(FACING)));
			if (!q.equals(pos)) level.setBlock(q, state.setValue(part(), p), 2);
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
		BlockPos main = mainPos(state, pos);
		int n = size();
		for (int p = 0; p < n * n * n; p++) {
			BlockPos q = main.offset(offset(p, state.getValue(FACING)));
			BlockState s = level.getBlockState(q);
			if (s.is(this) && s.getValue(part()) == p) level.setBlock(q, Blocks.AIR.defaultBlockState(), 3);
		}
		super.affectNeighborsAfterRemoval(state, level, pos, moved);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(mainPos(state, pos)) instanceof MechaAssemblerBlockEntity be) {
			player.openMenu(be);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(part()) == 0 ? new MechaAssemblerBlockEntity(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() || state.getValue(part()) != 0 ? null
			: createTickerHelper(type, ModBlockEntities.MECHA_ASSEMBLER, MechaAssemblerBlockEntity::tick);
	}

	// ===== Tamanhos =====
	public static class Small extends MechaAssemblerBlock {
		public static final MapCodec<Small> CODEC = simpleCodec(Small::new);
		public Small(Properties p) { super(p); }
		@Override public int size() { return 3; }
		@Override public IntegerProperty part() { return PART3; }
		@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	}

	public static class Medium extends MechaAssemblerBlock {
		public static final MapCodec<Medium> CODEC = simpleCodec(Medium::new);
		public Medium(Properties p) { super(p); }
		@Override public int size() { return 4; }
		@Override public IntegerProperty part() { return PART4; }
		@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	}

	public static class Large extends MechaAssemblerBlock {
		public static final MapCodec<Large> CODEC = simpleCodec(Large::new);
		public Large(Properties p) { super(p); }
		@Override public int size() { return 5; }
		@Override public IntegerProperty part() { return PART5; }
		@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	}
}
