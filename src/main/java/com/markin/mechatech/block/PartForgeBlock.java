package com.markin.mechatech.block;

import com.markin.mechatech.block.entity.ModBlockEntities;
import com.markin.mechatech.block.entity.PartForgeBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Forja de Peças: máquina 2x2x2 (versão quadrada e maior da cabine), com vidro na frente.
 * PART = 0..7 (x = PART&1 pra direita, y = (PART>>1)&1 pra cima, z = (PART>>2)&1 pra trás).
 * A parte 0 guarda a entidade (inventário, energia e animação).
 */
public class PartForgeBlock extends BaseEntityBlock {
	public static final MapCodec<PartForgeBlock> CODEC = simpleCodec(PartForgeBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, 7);
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public PartForgeBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, 0).setValue(LIT, false));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, PART, LIT);
	}

	// ===== Geometria local =====
	public static Direction right(Direction facing) { return facing.getClockWise(); }
	public static Direction back(Direction facing) { return facing.getOpposite(); }

	public static BlockPos offset(int part, Direction facing) {
		return BlockPos.ZERO.relative(right(facing), part & 1).above((part >> 1) & 1).relative(back(facing), (part >> 2) & 1);
	}

	public static BlockPos mainPos(BlockState state, BlockPos pos) {
		return pos.subtract(offset(state.getValue(PART), state.getValue(FACING)));
	}

	// ===== Colocar: ocupa 2x2x2, estende pra direita, pra cima e pra trás =====
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction facing = context.getHorizontalDirection().getOpposite();
		BlockPos main = context.getClickedPos().relative(right(facing), -1);
		Level level = context.getLevel();
		for (int p = 0; p < 8; p++) {
			BlockPos q = main.offset(offset(p, facing));
			if (q.getY() > level.getMaxY() || !level.getBlockState(q).canBeReplaced(context)) return null;
		}
		return this.defaultBlockState().setValue(FACING, facing).setValue(PART, 1);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		BlockPos main = mainPos(state, pos);
		for (int p = 0; p < 8; p++) {
			BlockPos q = main.offset(offset(p, state.getValue(FACING)));
			if (!q.equals(pos)) level.setBlock(q, state.setValue(PART, p), 3);
		}
	}

	/** Quebrou uma parte: some com as outras (só 1 item cai). */
	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		BlockPos main = mainPos(state, pos);
		for (int p = 0; p < 8; p++) {
			BlockPos q = main.offset(offset(p, state.getValue(FACING)));
			BlockState s = level.getBlockState(q);
			if (s.is(this) && s.getValue(PART) == p) level.setBlock(q, Blocks.AIR.defaultBlockState(), 3);
		}
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
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
		if (!level.isClientSide() && level.getBlockEntity(mainPos(state, pos)) instanceof PartForgeBlockEntity forge) {
			player.openMenu(forge);
		}
		return InteractionResult.SUCCESS;
	}

	/** Faíscas saindo do centro enquanto monta. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getValue(PART) != 0 || !state.getValue(LIT)) return;
		Direction f = state.getValue(FACING);
		Direction r = right(f), b = back(f);
		double cx = pos.getX() + 0.5 + 0.5 * (r.getStepX() + b.getStepX());
		double cz = pos.getZ() + 0.5 + 0.5 * (r.getStepZ() + b.getStepZ());
		double cy = pos.getY() + 1.0;
		for (int i = 0; i < 2; i++) {
			level.addParticle(ParticleTypes.ELECTRIC_SPARK, cx + (random.nextDouble() - 0.5) * 0.6, cy + (random.nextDouble() - 0.5) * 0.6,
				cz + (random.nextDouble() - 0.5) * 0.6, (random.nextDouble() - 0.5) * 0.1, 0.05, (random.nextDouble() - 0.5) * 0.1);
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(PART) == 0 ? new PartForgeBlockEntity(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() || state.getValue(PART) != 0 ? null
			: createTickerHelper(type, ModBlockEntities.PART_FORGE, PartForgeBlockEntity::tick);
	}
}
