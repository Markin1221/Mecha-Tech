package com.markin.mechatech.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public class CombustionGeneratorMenu extends MachineMenu {

	public CombustionGeneratorMenu(int id, Inventory inventory) {
		this(id, inventory, new SimpleContainer(1), new SimpleContainerData(4));
	}

	public CombustionGeneratorMenu(int id, Inventory inventory, Container container, ContainerData data) {
		super(ModMenus.COMBUSTION_GENERATOR, id, inventory, container, data, 1);
	}

	@Override
	protected void addMachineSlots() {
		this.addSlot(new FilteredSlot(this.container, 0, 80, 53));
	}

	public boolean isBurning() { return data.get(0) > 0; }

	/** 0..1 quanto do combustível atual ainda resta. */
	public float getBurnRatio() {
		int total = data.get(1);
		return total <= 0 ? 0 : (float) data.get(0) / total;
	}
}
