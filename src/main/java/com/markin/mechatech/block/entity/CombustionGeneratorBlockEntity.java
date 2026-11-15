package com.markin.mechatech.block.entity;

import com.markin.mechatech.block.MachineBlock;
import com.markin.mechatech.energy.EnergyHelper;
import com.markin.mechatech.energy.NeighborEnergy;
import com.markin.mechatech.menu.CombustionGeneratorMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Gerador a Combustão: queima qualquer combustível de fornalha e gera EM. */
public class CombustionGeneratorBlockEntity extends MachineBlockEntity implements MenuProvider {
	public static final int CAPACITY = 16000;
	public static final int GENERATION = 20;   // EM por tick queimando
	public static final int OUTPUT = 64;       // EM por tick por lado

	private final NeighborEnergy neighbors = new NeighborEnergy();
	private int burnTime;
	private int burnTimeTotal;

	public final ContainerData data = new ContainerData() {
		@Override
		public int get(int i) {
			return switch (i) {
				case 0 -> burnTime;
				case 1 -> burnTimeTotal;
				case 2 -> (int) energy.amount;
				case 3 -> CAPACITY;
				default -> 0;
			};
		}

		@Override
		public void set(int i, int v) {
			switch (i) {
				case 0 -> burnTime = v;
				case 1 -> burnTimeTotal = v;
				case 2 -> energy.amount = v;
				default -> { }
			}
		}

		@Override
		public int getCount() {
			return 4;
		}
	};

	public CombustionGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.COMBUSTION_GENERATOR, pos, state, 1, CAPACITY, 0, OUTPUT * 6);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, CombustionGeneratorBlockEntity be) {
		boolean wasBurning = be.burnTime > 0;
		boolean changed = false;

		if (be.burnTime > 0) {
			be.burnTime--;
			EnergyHelper.produce(be.energy, GENERATION);
			changed = true;
		}

		// Acende combustível novo se tiver espaço pra energia
		if (be.burnTime <= 0 && be.energy.amount < CAPACITY) {
			ItemStack fuel = be.items.get(0);
			int duration = level.fuelValues().burnDuration(fuel);
			if (duration > 0) {
				be.burnTime = duration;
				be.burnTimeTotal = duration;
				ItemStackTemplate remainder = fuel.getItem().getCraftingRemainder();
				fuel.shrink(1);
				if (fuel.isEmpty() && remainder != null) {
					be.items.set(0, remainder.create()); // balde de lava vira balde vazio
				}
				changed = true;
			}
		}

		if (be.energy.amount > 0 && level instanceof ServerLevel sl) {
			EnergyHelper.pushToNeighbors(sl, pos, be.neighbors, be.energy, OUTPUT);
		}

		boolean burning = be.burnTime > 0;
		if (wasBurning != burning) {
			level.setBlock(pos, state.setValue(MachineBlock.LIT, burning), 3);
		}
		if (changed) be.setChanged();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return this.level != null && this.level.fuelValues().isFuel(stack);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("BurnTime", burnTime);
		output.putInt("BurnTimeTotal", burnTimeTotal);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		burnTime = input.getIntOr("BurnTime", 0);
		burnTimeTotal = input.getIntOr("BurnTimeTotal", 0);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.mechatech.combustion_generator");
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new CombustionGeneratorMenu(id, inventory, this, this.data);
	}

}
