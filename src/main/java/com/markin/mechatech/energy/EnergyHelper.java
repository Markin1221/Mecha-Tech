package com.markin.mechatech.energy;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

/**
 * Energia do Mecha-Tech (EM) usa a API do Team Reborn Energy,
 * então funciona com Tech Reborn, Modern Industrialization e outros mods.
 */
public final class EnergyHelper {
	private EnergyHelper() {}

	/** Empurra energia para os vizinhos (geradores e cabos fazem isso). Retorna quanto saiu. */
	public static long pushToNeighbors(Level level, BlockPos pos, EnergyStorage source, long maxPerSide) {
		long total = 0;
		for (Direction dir : Direction.values()) {
			if (source.getAmount() <= 0) break;
			EnergyStorage target = EnergyStorage.SIDED.find(level, pos.relative(dir), dir.getOpposite());
			if (target == null || !target.supportsInsertion()) continue;
			total += EnergyStorageUtil.move(source, target, maxPerSide, null);
		}
		return total;
	}

	/** Tira energia de um armazenamento interno (sem limite de saída externa). */
	public static boolean consume(EnergyStorage storage, long amount) {
		try (Transaction tx = Transaction.openOuter()) {
			long taken = storage.extract(amount, tx);
			if (taken == amount) {
				tx.commit();
				return true;
			}
		}
		return false;
	}

	/** Coloca energia num armazenamento interno. */
	public static long produce(EnergyStorage storage, long amount) {
		try (Transaction tx = Transaction.openOuter()) {
			long added = storage.insert(amount, tx);
			tx.commit();
			return added;
		}
	}

	public static boolean hasStorage(Level level, BlockPos pos, Direction fromSide) {
		return EnergyStorage.SIDED.find(level, pos, fromSide) != null;
	}
}
