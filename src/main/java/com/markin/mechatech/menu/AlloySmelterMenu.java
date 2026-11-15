package com.markin.mechatech.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;

public class AlloySmelterMenu extends MachineMenu {

	public AlloySmelterMenu(int id, Inventory inventory) {
		this(id, inventory, new SimpleContainer(3), new SimpleContainerData(4));
	}

	public AlloySmelterMenu(int id, Inventory inventory, Container container, ContainerData data) {
		super(ModMenus.ALLOY_SMELTER, id, inventory, container, data, 3);
	}

	@Override
	protected void addMachineSlots() {
		this.addSlot(new Slot(this.container, 0, 44, 26));
		this.addSlot(new Slot(this.container, 1, 44, 48));
		this.addSlot(new OutputSlot(this.container, 2, 116, 37));
	}

	/** 0..1 progresso da liga atual. */
	public float getProgress() {
		int total = data.get(1);
		return total <= 0 ? 0 : Math.min(1f, (float) data.get(0) / total);
	}
}
