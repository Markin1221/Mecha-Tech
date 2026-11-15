package com.markin.mechatech.energy;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;

/**
 * Guarda em cache os 6 vizinhos de energia de um bloco.
 * Muito mais leve que procurar o vizinho do zero todo tick.
 */
public final class NeighborEnergy {
	@SuppressWarnings("unchecked")
	private final BlockApiCache<EnergyStorage, Direction>[] caches = new BlockApiCache[6];

	public BlockApiCache<EnergyStorage, Direction> cache(ServerLevel level, BlockPos pos, Direction dir) {
		int i = dir.ordinal();
		BlockApiCache<EnergyStorage, Direction> c = caches[i];
		if (c == null || c.getLevel() != level) {
			c = BlockApiCache.create(EnergyStorage.SIDED, level, pos.relative(dir));
			caches[i] = c;
		}
		return c;
	}

	/** Armazenamento de energia do vizinho naquele lado (ou null). */
	public EnergyStorage find(ServerLevel level, BlockPos pos, Direction dir) {
		return cache(level, pos, dir).find(dir.getOpposite());
	}

	public BlockEntity blockEntity(ServerLevel level, BlockPos pos, Direction dir) {
		return cache(level, pos, dir).getBlockEntity();
	}
}
