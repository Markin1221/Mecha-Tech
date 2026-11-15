package com.markin.mechatech.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Receita da Fundição de Ligas.
 * JSON: {"type":"mechatech:alloy_smelting","first":{"ingredient":"...","count":1},
 *        "second":{"ingredient":"...","count":2},"result":{...},"time":200}
 */
public record AlloySmeltingRecipe(CountedIngredient first, CountedIngredient second, ItemStackTemplate result, int time)
	implements Recipe<AlloyInput> {

	public record CountedIngredient(Ingredient ingredient, int count) {
		public static final Codec<CountedIngredient> CODEC = RecordCodecBuilder.create(i -> i.group(
			Ingredient.CODEC.fieldOf("ingredient").forGetter(CountedIngredient::ingredient),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(CountedIngredient::count)
		).apply(i, CountedIngredient::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, CountedIngredient> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, CountedIngredient::ingredient,
			ByteBufCodecs.VAR_INT, CountedIngredient::count,
			CountedIngredient::new);

		public boolean test(ItemStack stack) {
			return ingredient.test(stack) && stack.getCount() >= count;
		}
	}

	public static final MapCodec<AlloySmeltingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		CountedIngredient.CODEC.fieldOf("first").forGetter(AlloySmeltingRecipe::first),
		CountedIngredient.CODEC.fieldOf("second").forGetter(AlloySmeltingRecipe::second),
		ItemStackTemplate.CODEC.fieldOf("result").forGetter(AlloySmeltingRecipe::result),
		ExtraCodecs.POSITIVE_INT.optionalFieldOf("time", 200).forGetter(AlloySmeltingRecipe::time)
	).apply(i, AlloySmeltingRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, AlloySmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
		CountedIngredient.STREAM_CODEC, AlloySmeltingRecipe::first,
		CountedIngredient.STREAM_CODEC, AlloySmeltingRecipe::second,
		ItemStackTemplate.STREAM_CODEC, AlloySmeltingRecipe::result,
		ByteBufCodecs.VAR_INT, AlloySmeltingRecipe::time,
		AlloySmeltingRecipe::new);

	/** As entradas podem estar em qualquer um dos dois slots. */
	@Override
	public boolean matches(AlloyInput input, Level level) {
		return (first.test(input.first()) && second.test(input.second()))
			|| (first.test(input.second()) && second.test(input.first()));
	}

	/** true se o primeiro ingrediente da receita está no slot 0. */
	public boolean isInOrder(AlloyInput input) {
		return first.test(input.first()) && second.test(input.second());
	}

	@Override
	public ItemStack assemble(AlloyInput input) {
		return result.create();
	}

	@Override
	public boolean showNotification() {
		return false;
	}

	@Override
	public String group() {
		return "";
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeSerializer<AlloySmeltingRecipe> getSerializer() {
		return ModRecipes.ALLOY_SMELTING_SERIALIZER;
	}

	@Override
	public RecipeType<AlloySmeltingRecipe> getType() {
		return ModRecipes.ALLOY_SMELTING;
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return ModRecipes.MECHANICAL_BOOK_CATEGORY;
	}
}
