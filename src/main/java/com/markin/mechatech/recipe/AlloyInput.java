package com.markin.mechatech.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** As duas entradas da Fundição de Ligas. */
public record AlloyInput(ItemStack first, ItemStack second) implements RecipeInput {
	@Override
	public ItemStack getItem(int index) {
		return index == 0 ? first : second;
	}

	@Override
	public int size() {
		return 2;
	}
}
