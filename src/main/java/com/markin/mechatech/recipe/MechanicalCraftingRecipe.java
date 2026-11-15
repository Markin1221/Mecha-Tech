package com.markin.mechatech.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Receita com formato de até 4x4, usada só na Bancada Mecânica.
 * JSON: {"type":"mechatech:mechanical_crafting","pattern":[...],"key":{...},"result":{...}}
 */
public class MechanicalCraftingRecipe implements Recipe<CraftingInput> {
	public static final int MAX_SIZE = 4;

	private final List<String> pattern;
	private final Map<Character, Ingredient> key;
	private final ItemStackTemplate result;
	private final String group;

	private final int width;
	private final int height;
	private final List<Optional<Ingredient>> cells;

	public MechanicalCraftingRecipe(List<String> pattern, Map<Character, Ingredient> key, ItemStackTemplate result, String group) {
		this.pattern = pattern;
		this.key = key;
		this.result = result;
		this.group = group;
		// Corta linhas/colunas vazias em volta, igual o jogo faz com o que está na grade
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = -1, maxY = -1;
		for (int y = 0; y < pattern.size(); y++) {
			String row = pattern.get(y);
			for (int x = 0; x < row.length(); x++) {
				if (row.charAt(x) != ' ') {
					minX = Math.min(minX, x); maxX = Math.max(maxX, x);
					minY = Math.min(minY, y); maxY = Math.max(maxY, y);
				}
			}
		}
		this.width = maxX < 0 ? 0 : maxX - minX + 1;
		this.height = maxY < 0 ? 0 : maxY - minY + 1;
		this.cells = new ArrayList<>(width * height);
		for (int y = 0; y < height; y++) {
			String row = pattern.get(minY + y);
			for (int x = 0; x < width; x++) {
				int px = minX + x;
				char c = px < row.length() ? row.charAt(px) : ' ';
				this.cells.add(c == ' ' ? Optional.empty() : Optional.ofNullable(key.get(c)));
			}
		}
	}

	// ===== Validação do JSON =====
	private static DataResult<MechanicalCraftingRecipe> validate(MechanicalCraftingRecipe r) {
		if (r.pattern.isEmpty() || r.pattern.size() > MAX_SIZE) {
			return DataResult.error(() -> "Pattern precisa ter de 1 a 4 linhas");
		}
		int w = r.pattern.get(0).length();
		for (String row : r.pattern) {
			if (row.length() != w || w == 0 || w > MAX_SIZE) {
				return DataResult.error(() -> "Todas as linhas precisam ter o mesmo tamanho (1 a 4)");
			}
			for (char c : row.toCharArray()) {
				if (c != ' ' && !r.key.containsKey(c)) {
					return DataResult.error(() -> "Símbolo '" + c + "' não está na key");
				}
			}
		}
		return DataResult.success(r);
	}

	private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap(
		s -> s.length() == 1 && s.charAt(0) != ' '
			? DataResult.success(s.charAt(0))
			: DataResult.error(() -> "Símbolo inválido: '" + s + "'"),
		String::valueOf);

	public static final MapCodec<MechanicalCraftingRecipe> MAP_CODEC = RecordCodecBuilder.<MechanicalCraftingRecipe>mapCodec(i -> i.group(
		Codec.STRING.listOf().fieldOf("pattern").forGetter(r -> r.pattern),
		Codec.unboundedMap(SYMBOL_CODEC, Ingredient.CODEC).fieldOf("key").forGetter(r -> r.key),
		ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
		Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group)
	).apply(i, MechanicalCraftingRecipe::new)).validate(MechanicalCraftingRecipe::validate);

	private static final StreamCodec<io.netty.buffer.ByteBuf, Character> CHAR_STREAM_CODEC =
		ByteBufCodecs.VAR_INT.map(v -> (char) v.intValue(), c -> (int) c);

	public static final StreamCodec<RegistryFriendlyByteBuf, MechanicalCraftingRecipe> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), r -> r.pattern,
		ByteBufCodecs.map(HashMap::new, CHAR_STREAM_CODEC, Ingredient.CONTENTS_STREAM_CODEC), r -> new HashMap<>(r.key),
		ItemStackTemplate.STREAM_CODEC, r -> r.result,
		ByteBufCodecs.STRING_UTF8, r -> r.group,
		MechanicalCraftingRecipe::new);

	// ===== Lógica da receita =====
	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (input.width() != width || input.height() != height) return false;
		return matchesGrid(input, false) || matchesGrid(input, true);
	}

	private boolean matchesGrid(CraftingInput input, boolean mirrored) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int px = mirrored ? width - 1 - x : x;
				Optional<Ingredient> ing = cells.get(px + y * width);
				if (!Ingredient.testOptionalIngredient(ing, input.getItem(x, y))) return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return result.create();
	}

	public ItemStack getResultPreview() {
		return result.create();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public boolean showNotification() {
		return false;
	}

	@Override
	public String group() {
		return group;
	}

	@Override
	public RecipeSerializer<MechanicalCraftingRecipe> getSerializer() {
		return ModRecipes.MECHANICAL_CRAFTING_SERIALIZER;
	}

	@Override
	public RecipeType<MechanicalCraftingRecipe> getType() {
		return ModRecipes.MECHANICAL_CRAFTING;
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
