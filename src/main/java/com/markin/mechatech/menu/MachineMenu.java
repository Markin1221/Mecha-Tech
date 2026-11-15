package com.markin.mechatech.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Base dos menus de máquina: slots da máquina + inventário + dados sincronizados. */
public abstract class MachineMenu extends AbstractContainerMenu {
	protected final Container container;
	protected final ContainerData data;
	private final int machineSlots;

	protected MachineMenu(MenuType<?> type, int id, Inventory inventory, Container container, ContainerData data, int machineSlots) {
		super(type, id);
		checkContainerSize(container, machineSlots);
		checkContainerDataCount(data, 4);
		this.container = container;
		this.data = data;
		this.machineSlots = machineSlots;
		container.startOpen(inventory.player);
		addMachineSlots();
		this.addStandardInventorySlots(inventory, 8, 84);
		this.addDataSlots(data);
	}

	protected abstract void addMachineSlots();

	public int getEnergy() { return data.get(2); }
	public int getCapacity() { return Math.max(1, data.get(3)); }

	/** 0..1 */
	public float getEnergyRatio() { return Math.min(1f, (float) getEnergy() / getCapacity()); }

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.container.stopOpen(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		ItemStack copy = stack.copy();
		int invStart = machineSlots, invEnd = machineSlots + 36;

		if (index < machineSlots) {
			if (!this.moveItemStackTo(stack, invStart, invEnd, true)) return ItemStack.EMPTY;
		} else if (!this.moveItemStackTo(stack, 0, machineSlots, false)) {
			if (index < invStart + 27) {
				if (!this.moveItemStackTo(stack, invStart + 27, invEnd, false)) return ItemStack.EMPTY;
			} else if (!this.moveItemStackTo(stack, invStart, invStart + 27, false)) {
				return ItemStack.EMPTY;
			}
		}
		if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
		if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
		slot.onTake(player, stack);
		return copy;
	}

	/** Slot que só aceita o que o container deixa (ex: combustível). */
	protected static class FilteredSlot extends Slot {
		public FilteredSlot(Container c, int i, int x, int y) { super(c, i, x, y); }
		@Override public boolean mayPlace(ItemStack stack) { return this.container.canPlaceItem(this.getContainerSlot(), stack); }
	}

	/** Slot de saída: não aceita itens. */
	protected static class OutputSlot extends Slot {
		public OutputSlot(Container c, int i, int x, int y) { super(c, i, x, y); }
		@Override public boolean mayPlace(ItemStack stack) { return false; }
	}
}
