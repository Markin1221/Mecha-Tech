package com.markin.mechatech.block;

import com.markin.mechatech.block.entity.AlloySmelterBlockEntity;
import com.markin.mechatech.block.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AlloySmelterBlock extends MachineBlock {
	public static final MapCodec<AlloySmelterBlock> CODEC = simpleCodec(AlloySmelterBlock::new);

	public AlloySmelterBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AlloySmelterBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null
			: createTickerHelper(type, ModBlockEntities.ALLOY_SMELTER, AlloySmelterBlockEntity::tick);
	}

	/** Brilho de metal derretido saindo pela frente. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) return;
		Direction f = state.getValue(FACING);
		double x = pos.getX() + 0.5 + f.getStepX() * 0.52;
		double z = pos.getZ() + 0.5 + f.getStepZ() * 0.52;
		double y = pos.getY() + 0.3 + random.nextDouble() * 0.3;
		level.addParticle(ParticleTypes.SMALL_FLAME, x + (random.nextDouble() - 0.5) * 0.4 * Math.abs(f.getStepZ()), y,
			z + (random.nextDouble() - 0.5) * 0.4 * Math.abs(f.getStepX()), 0, 0, 0);
	}
}
