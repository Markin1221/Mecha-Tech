package com.example.items;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import com.example.ExampleMod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;


public class modToolsMaterial {
    public static final @NonNull TagKey<Block> INCORRECT_FOR_MINER_ARM_TOOL = TagKey.create(
        BuiltInRegistries.BLOCK.key(),
         Identifier.fromNamespaceAndPath(ExampleMod.MOD_ID, "incorrect_for_miner_arm_tool"));
    public static final @NonNull TagKey<Item> REPAIRS_MINER_ARM = TagKey.create(
		BuiltInRegistries.ITEM.key(),
		Identifier.fromNamespaceAndPath(ExampleMod.MOD_ID, "repairs_miner_arm")
	);

    public static final ToolMaterial MINER_ARM_TOOL_MATERIAL = new ToolMaterial(
        INCORRECT_FOR_MINER_ARM_TOOL,
        3000,
        12.0f ,
        12.0f,
        30,
        REPAIRS_MINER_ARM);

}
