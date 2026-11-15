package com.markin.mechatech.items;

import com.markin.mechatech.MechaTech;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

public class ModToolMaterials {

	public static final TagKey<Item> TITANIUM_TOOL_MATERIALS =
		TagKey.create(Registries.ITEM, MechaTech.id("titanium_tool_materials"));

	/** Blocos que a Furadeira quebra (picareta + pá). */
	public static final TagKey<Block> MINEABLE_WITH_DRILL =
		TagKey.create(Registries.BLOCK, MechaTech.id("mineable/drill"));

	/** Blocos que a Motosserra quebra (machado + folhas). */
	public static final TagKey<Block> MINEABLE_WITH_CHAINSAW =
		TagKey.create(Registries.BLOCK, MechaTech.id("mineable/chainsaw"));

	/** Titânio: nível de ferro, mais durável e rápido. */
	public static final ToolMaterial TITANIUM = new ToolMaterial(
		BlockTags.INCORRECT_FOR_IRON_TOOL, 900, 7.5F, 2.5F, 14, TITANIUM_TOOL_MATERIALS);
}
