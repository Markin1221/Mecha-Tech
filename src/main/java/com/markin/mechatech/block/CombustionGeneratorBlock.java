package com.markin.mechatech.block;

import com.markin.mechatech.block.entity.CombustionGeneratorBlockEntity;
import com.markin.mechatech.block.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CombustionGeneratorBlock extends MachineBlock {
	public static final MapCodec<CombustionGeneratorBlock> CODEC = simpleCodec(CombustionGeneratorBlock::new);

	public CombustionGeneratorBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CombustionGeneratorBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null
			: createTickerHelper(type, ModBlockEntities.COMBUSTION_GENERATOR, CombustionGeneratorBlockEntity::tick);
	}

	/** Fumaça e fagulhas quando está queimando. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) return;
		double x = pos.getX() + 0.5, y = pos.getY() + 1.0, z = pos.getZ() + 0.5;
		level.addParticle(ParticleTypes.SMOKE, x + (random.nextDouble() - 0.5) * 0.3, y, z + (random.nextDouble() - 0.5) * 0.3, 0, 0.05, 0);
		if (random.nextInt(4) == 0) {
			level.addParticle(ParticleTypes.FLAME, x + (random.nextDouble() - 0.5) * 0.2, y, z + (random.nextDouble() - 0.5) * 0.2, 0, 0.02, 0);
		}
		if (random.nextInt(30) == 0) {
			level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.6F, 0.8F, false);
		}
	}
}
