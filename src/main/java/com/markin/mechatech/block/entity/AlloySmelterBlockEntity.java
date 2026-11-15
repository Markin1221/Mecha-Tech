package com.markin.mechatech.block.entity;

import java.util.Optional;

import com.markin.mechatech.block.MachineBlock;
import com.markin.mechatech.energy.EnergyHelper;
import com.markin.mechatech.menu.AlloySmelterMenu;
import com.markin.mechatech.recipe.AlloyInput;
import com.markin.mechatech.recipe.AlloySmeltingRecipe;
import com.markin.mechatech.recipe.ModRecipes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Fundição de Ligas: 2 entradas + 1 saída, gasta EM. Slots: 0,1 = entradas, 2 = saída. */
public class AlloySmelterBlockEntity extends MachineBlockEntity implements MenuProvider {
	public static final int CAPACITY = 16000;
	public static final int USAGE = 16;      // EM por tick trabalhando
	public static final int INPUT = 128;     // EM por tick que aceita

	private int progress;
	// Cache da receita: só procura de novo quando as entradas mudam
	private ItemStack cachedA = ItemStack.EMPTY, cachedB = ItemStack.EMPTY;
	private Optional<RecipeHolder<AlloySmeltingRecipe>> cachedRecipe = Optional.empty();
	private int progressTotal = 200;

	public final ContainerData data = new ContainerData() {
		@Override
		public int get(int i) {
			return switch (i) {
				case 0 -> progress;
				case 1 -> progressTotal;
				case 2 -> (int) energy.amount;
				case 3 -> CAPACITY;
				default -> 0;
			};
		}

		@Override
		public void set(int i, int v) {
			switch (i) {
				case 0 -> progress = v;
				case 1 -> progressTotal = v;
				case 2 -> energy.amount = v;
				default -> { }
			}
		}

		@Override
		public int getCount() {
			return 4;
		}
	};

	public AlloySmelterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ALLOY_SMELTER, pos, state, 3, CAPACITY, INPUT, 0);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, AlloySmelterBlockEntity be) {
		if (!(level instanceof ServerLevel serverLevel)) return;

		AlloyInput input = new AlloyInput(be.items.get(0), be.items.get(1));
		Optional<RecipeHolder<AlloySmeltingRecipe>> recipe = be.findRecipe(serverLevel, input);

		boolean working = false;
		if (recipe.isPresent() && be.canOutput(recipe.get().value().assemble(input))) {
			AlloySmeltingRecipe r = recipe.get().value();
			be.progressTotal = r.time();
			if (EnergyHelper.consume(be.energy, USAGE)) {
				working = true;
				be.progress++;
				if (be.progress >= be.progressTotal) {
					be.craft(r, input);
					be.progress = 0;
				}
				be.setChanged();
			}
		} else if (be.progress != 0) {
			be.progress = 0;
			be.setChanged();
		}

		if (state.getValue(MachineBlock.LIT) != working) {
			level.setBlock(pos, state.setValue(MachineBlock.LIT, working), 3);
		}
	}

	private Optional<RecipeHolder<AlloySmeltingRecipe>> findRecipe(ServerLevel level, AlloyInput input) {
		if (input.first().isEmpty() || input.second().isEmpty()) {
			cachedA = ItemStack.EMPTY;
			cachedB = ItemStack.EMPTY;
			return cachedRecipe = Optional.empty();
		}
		boolean same = ItemStack.isSameItemSameComponents(input.first(), cachedA) && input.first().getCount() == cachedA.getCount()
			&& ItemStack.isSameItemSameComponents(input.second(), cachedB) && input.second().getCount() == cachedB.getCount();
		if (!same) {
			cachedA = input.first().copy();
			cachedB = input.second().copy();
			cachedRecipe = level.recipeAccess().getRecipeFor(ModRecipes.ALLOY_SMELTING, input, level);
		}
		return cachedRecipe;
	}

	private boolean canOutput(ItemStack result) {
		ItemStack out = this.items.get(2);
		if (out.isEmpty()) return true;
		return ItemStack.isSameItemSameComponents(out, result)
			&& out.getCount() + result.getCount() <= out.getMaxStackSize();
	}

	private void craft(AlloySmeltingRecipe r, AlloyInput input) {
		ItemStack result = r.assemble(input);
		boolean inOrder = r.isInOrder(input);
		this.items.get(0).shrink(inOrder ? r.first().count() : r.second().count());
		this.items.get(1).shrink(inOrder ? r.second().count() : r.first().count());
		ItemStack out = this.items.get(2);
		if (out.isEmpty()) {
			this.items.set(2, result);
		} else {
			out.grow(result.getCount());
		}
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return slot != 2;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("Progress", progress);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		progress = input.getIntOr("Progress", 0);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.mechatech.alloy_smelter");
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new AlloySmelterMenu(id, inventory, this, this.data);
	}
}
