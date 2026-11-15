package com.markin.mechatech.menu;

import java.util.Optional;

import com.markin.mechatech.block.ModBlocks;
import com.markin.mechatech.recipe.MechanicalCraftingRecipe;
import com.markin.mechatech.recipe.ModRecipes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Menu da Bancada Mecânica: grade 4x4 + resultado.
 * Slots: 0 = resultado, 1..16 = grade, 17..43 = inventário, 44..52 = hotbar.
 */
public class MechanicalWorkbenchMenu extends AbstractContainerMenu {
	public static final int GRID = 4;
	private static final int RESULT_SLOT = 0;
	private static final int GRID_START = 1;
	private static final int GRID_END = GRID_START + GRID * GRID; // exclusivo
	private static final int INV_START = GRID_END;
	private static final int HOTBAR_START = INV_START + 27;
	private static final int HOTBAR_END = HOTBAR_START + 9;

	private final CraftingContainer craftSlots = new TransientCraftingContainer(this, GRID, GRID);
	private final ResultContainer resultSlots = new ResultContainer();
	private final ContainerLevelAccess access;
	private final Player player;

	// Construtor do cliente
	public MechanicalWorkbenchMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, ContainerLevelAccess.NULL);
	}

	// Construtor do servidor
	public MechanicalWorkbenchMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
		super(ModMenus.MECHANICAL_WORKBENCH, containerId);
		this.access = access;
		this.player = inventory.player;

		this.addSlot(new ResultSlot(this.resultSlots, 0, 134, 44));

		for (int row = 0; row < GRID; row++) {
			for (int col = 0; col < GRID; col++) {
				this.addSlot(new Slot(this.craftSlots, col + row * GRID, 26 + col * 18, 17 + row * 18));
			}
		}

		this.addStandardInventorySlots(inventory, 8, 102);
	}

	@Override
	public void slotsChanged(Container container) {
		this.access.execute((level, pos) -> {
			if (level instanceof ServerLevel serverLevel) {
				updateResult(serverLevel);
			}
		});
	}

	private void updateResult(ServerLevel level) {
		CraftingInput input = this.craftSlots.asCraftInput();
		ItemStack out = ItemStack.EMPTY;
		Optional<RecipeHolder<MechanicalCraftingRecipe>> recipe =
			level.recipeAccess().getRecipeFor(ModRecipes.MECHANICAL_CRAFTING, input, level);
		if (recipe.isPresent()) {
			out = recipe.get().value().assemble(input);
		}
		this.resultSlots.setItem(0, out);
		this.broadcastChanges();
	}

	/** Consome 1 de cada item da grade (devolvendo "restos", tipo balde vazio). */
	private void consumeIngredients(Player taker) {
		for (int i = 0; i < this.craftSlots.getContainerSize(); i++) {
			ItemStack stack = this.craftSlots.getItem(i);
			if (stack.isEmpty()) continue;
			ItemStackTemplate remainder = stack.getItem().getCraftingRemainder();
			this.craftSlots.removeItem(i, 1);
			if (remainder != null) {
				ItemStack rest = remainder.create();
				if (this.craftSlots.getItem(i).isEmpty()) {
					this.craftSlots.setItem(i, rest);
				} else if (!taker.getInventory().add(rest)) {
					taker.drop(rest, false);
				}
			}
		}
		this.slotsChanged(this.craftSlots);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.access.execute((level, pos) -> this.clearContainer(player, this.craftSlots));
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, ModBlocks.MECHANICAL_WORKBENCH);
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack copy = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

		ItemStack stack = slot.getItem();
		copy = stack.copy();

		if (index == RESULT_SLOT) {
			// Shift-click no resultado: fabrica o máximo possível
			int crafted = 0;
			while (crafted < 64) {
				ItemStack result = this.resultSlots.getItem(0);
				if (result.isEmpty() || !result.is(copy.getItem())) break;
				ItemStack toMove = result.copy();
				if (!this.moveItemStackTo(toMove, INV_START, HOTBAR_END, true)) break;
				crafted += result.getCount();
				consumeIngredients(player);
			}
			return ItemStack.EMPTY;
		} else if (index >= INV_START && index < HOTBAR_END) {
			if (!this.moveItemStackTo(stack, GRID_START, GRID_END, false)) {
				if (index < HOTBAR_START) {
					if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) return ItemStack.EMPTY;
				} else if (!this.moveItemStackTo(stack, INV_START, HOTBAR_START, false)) {
					return ItemStack.EMPTY;
				}
			}
		} else if (!this.moveItemStackTo(stack, INV_START, HOTBAR_END, false)) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
		slot.onTake(player, stack);
		return copy;
	}

	/** Slot de resultado: não aceita itens; ao pegar, consome a grade. */
	private class ResultSlot extends Slot {
		ResultSlot(Container container, int index, int x, int y) {
			super(container, index, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public void onTake(Player taker, ItemStack stack) {
			stack.onCraftedBy(taker, stack.getCount());
			consumeIngredients(taker);
			super.onTake(taker, stack);
		}
	}
}
