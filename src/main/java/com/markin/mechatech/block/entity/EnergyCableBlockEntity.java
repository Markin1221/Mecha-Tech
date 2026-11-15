package com.markin.mechatech.block.entity;


import net.minecraft.core.BlockPos;
import com.markin.mechatech.energy.NeighborEnergy;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleEnergyStorage;

/**
 * Cabo de Energia: guarda um pouquinho de EM, entrega pras máquinas
 * e equilibra com os cabos vizinhos (a energia "escorre" do cheio pro vazio).
 */
public class EnergyCableBlockEntity extends BlockEntity {
	public static final int BUFFER = 1000;
	public static final int RATE = 128;

	public final SimpleEnergyStorage energy = new SimpleEnergyStorage(BUFFER, RATE, RATE) {
		@Override
		protected void onFinalCommit() {
			setChanged();
		}
	};

	public EnergyCableBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ENERGY_CABLE, pos, state);
	}

	private final NeighborEnergy neighbors = new NeighborEnergy();

	public static void tick(Level level, BlockPos pos, BlockState state, EnergyCableBlockEntity be) {
		if (be.energy.amount <= 0 || !(level instanceof ServerLevel sl)) return;

		for (Direction dir : Direction.values()) {
			if (be.energy.amount <= 0) break;
			EnergyStorage target = be.neighbors.find(sl, pos, dir);
			if (target == null || !target.supportsInsertion()) continue;
			if (be.neighbors.blockEntity(sl, pos, dir) instanceof EnergyCableBlockEntity other) {
				// Equilibra com o cabo vizinho (a energia escorre do cheio pro vazio)
				long diff = be.energy.amount - other.energy.amount;
				if (diff > 1) EnergyStorageUtil.move(be.energy, other.energy, Math.min(diff / 2, RATE), null);
			} else {
				EnergyStorageUtil.move(be.energy, target, RATE, null);
			}
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putLong("Energy", energy.amount);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		energy.amount = Math.min(input.getLongOr("Energy", 0L), BUFFER);
	}

}
