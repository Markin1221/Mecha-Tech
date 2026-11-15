package com.markin.mechatech.menu;

import java.util.List;

import com.markin.mechatech.block.entity.PartForgeBlockEntity;
import com.markin.mechatech.mecha.MechaParts;
import com.markin.mechatech.mecha.PartType;
import com.markin.mechatech.mecha.PartVariant;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu da Forja de Peças. Slots 0..5 = materiais, 6 = saída, 7..42 = inventário.
 * Botões: 0/1 tipo, 2/3 variante, 4/5 escala.
 */
public class PartForgeMenu extends AbstractContainerMenu {
	private final Container container;
	private final ContainerData data;
	private final PartForgeBlockEntity forge; // null no cliente

	public PartForgeMenu(int id, Inventory inventory) {
		this(id, inventory, new SimpleContainer(7), new SimpleContainerData(8), null);
	}

	public PartForgeMenu(int id, Inventory inventory, Container container, ContainerData data, PartForgeBlockEntity forge) {
		super(ModMenus.PART_FORGE, id);
		checkContainerSize(container, 7);
		checkContainerDataCount(data, 8);
		this.container = container;
		this.data = data;
		this.forge = forge;
		for (int i = 0; i < 6; i++) {
			this.addSlot(new Slot(container, i, 12 + (i % 3) * 18, 105 + (i / 3) * 18));
		}
		this.addSlot(new Slot(container, PartForgeBlockEntity.OUTPUT_SLOT, 113, 111) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		});
		this.addStandardInventorySlots(inventory, 8, 152);
		this.addDataSlots(data);
	}

	public float progress() { return data.get(1) <= 0 ? 0 : Math.min(1f, (float) data.get(0) / data.get(1)); }
	public int energy() { return data.get(2) * 2; }
	public int capacity() { return Math.max(1, data.get(3) * 2); }
	public int scale() { return data.get(6); }
	public boolean formed() { return data.get(7) != 0; }

	public PartType type() { return PartType.values()[Math.floorMod(data.get(4), PartType.values().length)]; }

	public PartVariant variant() {
		List<PartVariant> list = MechaParts.of(type());
		return list.isEmpty() ? null : list.get(Math.floorMod(data.get(5), list.size()));
	}

	/** Quantos materiais do custo estão nos slots (calculado no cliente pelos slots sincronizados). */
	public int countOf(PartVariant.Cost cost) {
		int n = 0;
		for (int i = 0; i < 6; i++) {
			ItemStack s = this.slots.get(i).getItem();
			if (!s.isEmpty() && cost.ingredient().test(s)) n += s.getCount();
		}
		return n;
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (forge == null || id < 0 || id > 5) return false;
		forge.changeSelection(id);
		return true;
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
		} else if (!this.moveItemStackTo(stack, 0, 6, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
		if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
		slot.onTake(player, stack);
		return copy;
	}
}
