package com.example.items;

import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.resources.Identifier;

import com.example.ExampleMod;


public class ModItems {

	public static final Item RUBY = register(
		"ruby",
		Item::new,
		new Item.Properties()
	);

	public static final Item MAGIC_WAND = register(
		"magic_wand",
		Item::new,
		new Item.Properties().stacksTo(1) 
	);

	public static final Item minerArm = register(
		"miner_arm",
		MinerArmItem::new,
		new Item.Properties().stacksTo(1));

	public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ExampleMod.MOD_ID, name));
		T item = itemFactory.apply(settings.setId(itemKey));
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);
		return item;
	}

	public static void initialize() {
		// Get the event for modifying entries in the ingredients group.
// And register an event handler that adds our suspicious item to the ingredients group.
CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
		.register((creativeTab) -> creativeTab.accept(ModItems.minerArm));
	}
}