package com.markin.mechatech.recipe;

import com.markin.mechatech.MechaTech;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipes {

	public static final RecipeType<MechanicalCraftingRecipe> MECHANICAL_CRAFTING = Registry.register(
		BuiltInRegistries.RECIPE_TYPE, MechaTech.id("mechanical_crafting"),
		new RecipeType<MechanicalCraftingRecipe>() {
			@Override
			public String toString() {
				return "mechatech:mechanical_crafting";
			}
		});

	public static final RecipeSerializer<MechanicalCraftingRecipe> MECHANICAL_CRAFTING_SERIALIZER = Registry.register(
		BuiltInRegistries.RECIPE_SERIALIZER, MechaTech.id("mechanical_crafting"),
		new RecipeSerializer<>(MechanicalCraftingRecipe.MAP_CODEC, MechanicalCraftingRecipe.STREAM_CODEC));

	public static final RecipeBookCategory MECHANICAL_BOOK_CATEGORY = Registry.register(
		BuiltInRegistries.RECIPE_BOOK_CATEGORY, MechaTech.id("mechanical_crafting"), new RecipeBookCategory());

	public static final RecipeType<AlloySmeltingRecipe> ALLOY_SMELTING = Registry.register(
		BuiltInRegistries.RECIPE_TYPE, MechaTech.id("alloy_smelting"),
		new RecipeType<AlloySmeltingRecipe>() {
			@Override
			public String toString() {
				return "mechatech:alloy_smelting";
			}
		});

	public static final RecipeSerializer<AlloySmeltingRecipe> ALLOY_SMELTING_SERIALIZER = Registry.register(
		BuiltInRegistries.RECIPE_SERIALIZER, MechaTech.id("alloy_smelting"),
		new RecipeSerializer<>(AlloySmeltingRecipe.MAP_CODEC, AlloySmeltingRecipe.STREAM_CODEC));

	public static void initialize() {
		// Carrega a classe e registra tudo.
	}
}
