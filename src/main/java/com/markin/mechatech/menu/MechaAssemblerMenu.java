package com.markin.mechatech.menu;

import com.markin.mechatech.block.entity.MechaAssemblerBlockEntity;
import com.markin.mechatech.component.ModComponents;
import com.markin.mechatech.items.ModItems;
import com.markin.mechatech.mecha.MechaData;
import com.markin.mechatech.mecha.MechaPartData;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Menu da Montadora. Slots 0..5 peças, 6 mecha, 7..42 inventário. */
public class MechaAssemblerMenu extends AbstractContainerMenu {
	/** Posição dos slots de peça (cabeça, torso, braço E, braço D, pernas, mochila). */
	public static final int[][] POS = {{43, 20}, {43, 42}, {21, 42}, {65, 42}, {43, 64}, {65, 20}};
	public static final int OUT_X = 125, OUT_Y = 42;

	private final Container container;
	private final ContainerData data;

	public MechaAssemblerMenu(int id, Inventory inventory) {
		this(id, inventory, new SimpleContainer(7), new SimpleContainerData(6));
	}

	public MechaAssemblerMenu(int id, Inventory inventory, Container container, ContainerData data) {
		super(ModMenus.MECHA_ASSEMBLER, id);
		checkContainerSize(container, 7);
		checkContainerDataCount(data, 6);
		this.container = container;
		this.data = data;
		for (int i = 0; i < 6; i++) {
			final int slot = i;
			this.addSlot(new Slot(container, i, POS[i][0], POS[i][1]) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return MechaAssemblerBlockEntity.accepts(slot, stack);
				}

				@Override
				public int getMaxStackSize() {
					return 1;
				}
			});
		}
		this.addSlot(new Slot(container, MechaAssemblerBlockEntity.OUTPUT, OUT_X, OUT_Y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(ModItems.MECHA);
			}
		});
		this.addStandardInventorySlots(inventory, 8, 140);
		this.addDataSlots(data);
	}

	public float progress() { return data.get(1) <= 0 ? 0 : Math.min(1f, (float) data.get(0) / data.get(1)); }
	public int energy() { return data.get(2) * 10; }
	public int capacity() { return Math.max(1, data.get(3) * 10); }
	public int status() { return data.get(4); }
	public int size() { return data.get(5); }

	/** Mecha montado com as peças que estão nos slots agora (lado do cliente). */
	public MechaData preview() {
		MechaPartData[] p = new MechaPartData[6];
		for (int i = 0; i < 6; i++) {
			MechaPartData d = this.slots.get(i).getItem().get(ModComponents.MECHA_PART);
			p[i] = d == null || d.def() == null ? MechaData.NONE : d;
		}
		return new MechaData(p[0], p[1], p[2], p[3], p[4], p[5], 0);
	}

	@Override
	public boolean stillValid(Player player) {
		return container.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		ItemStack copy = stack.copy();
		if (index < 7) {
			if (!this.moveItemStackTo(stack, 7, 43, true)) return ItemStack.EMPTY;
		} else {
			boolean moved = false;
			for (int i = 0; i < 7 && !moved; i++) {
				Slot s = this.slots.get(i);
				if (!s.hasItem() && s.mayPlace(stack)) moved = this.moveItemStackTo(stack, i, i + 1, false);
			}
			if (!moved) return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
		if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
		slot.onTake(player, stack);
		return copy;
	}
}
