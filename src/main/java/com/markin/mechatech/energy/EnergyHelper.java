package com.markin.mechatech.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleEnergyStorage;

/**
 * Energia do Mecha-Tech (EM) usa a API do Team Reborn Energy,
 * então funciona com Tech Reborn, Modern Industrialization e outros mods.
 */
public final class EnergyHelper {
	private EnergyHelper() {}

	/** Empurra energia para os vizinhos (geradores fazem isso). Retorna quanto saiu. */
	public static long pushToNeighbors(ServerLevel level, BlockPos pos, NeighborEnergy neighbors, EnergyStorage source, long maxPerSide) {
		long total = 0;
		for (Direction dir : Direction.values()) {
			if (source.getAmount() <= 0) break;
			EnergyStorage target = neighbors.find(level, pos, dir);
			if (target == null || !target.supportsInsertion()) continue;
			total += EnergyStorageUtil.move(source, target, maxPerSide, null);
		}
		return total;
	}

	/**
	 * Gasta energia interna da máquina. Mexe direto no valor porque o limite de
	 * entrada/saída (maxInsert/maxExtract) vale só pra transferência entre blocos.
	 */
	public static boolean consume(SimpleEnergyStorage storage, long amount) {
		if (storage.amount < amount) return false;
		storage.amount -= amount;
		return true;
	}

	/** Gera energia interna (gerador). Retorna quanto coube. */
	public static long produce(SimpleEnergyStorage storage, long amount) {
		long added = Math.min(amount, storage.capacity - storage.amount);
		storage.amount += added;
		return added;
	}

	public static boolean hasStorage(Level level, BlockPos pos, Direction fromSide) {
		return EnergyStorage.SIDED.find(level, pos, fromSide) != null;
	}
}
