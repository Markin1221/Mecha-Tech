package com.markin.mechatech.block;

import java.util.Map;

import com.markin.mechatech.block.entity.EnergyCableBlockEntity;
import com.markin.mechatech.block.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.reborn.energy.api.EnergyStorage;

/** Cabo de Energia: conecta sozinho em outros cabos e em qualquer bloco com energia. */
public class EnergyCableBlock extends BaseEntityBlock {
	public static final MapCodec<EnergyCableBlock> CODEC = simpleCodec(EnergyCableBlock::new);
	public static final Map<Direction, BooleanProperty> PROPS = PipeBlock.PROPERTY_BY_DIRECTION;

	private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
	private static final Map<Direction, VoxelShape> ARMS = Map.of(
		Direction.NORTH, Block.box(5, 5, 0, 11, 11, 5),
		Direction.SOUTH, Block.box(5, 5, 11, 11, 11, 16),
		Direction.WEST, Block.box(0, 5, 5, 5, 11, 11),
		Direction.EAST, Block.box(11, 5, 5, 16, 11, 11),
		Direction.DOWN, Block.box(5, 0, 5, 11, 5, 11),
		Direction.UP, Block.box(5, 11, 5, 11, 16, 11));

	public EnergyCableBlock(Properties properties) {
		super(properties);
		BlockState s = this.stateDefinition.any();
		for (BooleanProperty p : PROPS.values()) s = s.setValue(p, false);
		this.registerDefaultState(s);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		PROPS.values().forEach(builder::add);
	}

	private boolean connects(LevelReader reader, BlockPos pos, Direction dir) {
		BlockPos np = pos.relative(dir);
		if (reader.getBlockState(np).getBlock() instanceof EnergyCableBlock) return true;
		if (reader instanceof Level level) {
			return EnergyStorage.SIDED.find(level, np, dir.getOpposite()) != null;
		}
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState s = this.defaultBlockState();
		for (Direction d : Direction.values()) {
			s = s.setValue(PROPS.get(d), connects(context.getLevel(), context.getClickedPos(), d));
		}
		return s;
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
									 Direction dir, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		return state.setValue(PROPS.get(dir), connects(level, pos, dir));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		VoxelShape shape = CORE;
		for (Direction d : Direction.values()) {
			if (state.getValue(PROPS.get(d))) shape = Shapes.or(shape, ARMS.get(d));
		}
		return shape;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new EnergyCableBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null
			: createTickerHelper(type, ModBlockEntities.ENERGY_CABLE, EnergyCableBlockEntity::tick);
	}
}
