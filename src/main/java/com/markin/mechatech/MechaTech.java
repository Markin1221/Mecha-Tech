package com.markin.mechatech;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.markin.mechatech.block.ModBlocks;
import com.markin.mechatech.block.entity.ModBlockEntities;
import com.markin.mechatech.items.ModCreativeTab;
import com.markin.mechatech.items.ModItems;
import com.markin.mechatech.menu.ModMenus;
import com.markin.mechatech.recipe.ModRecipes;
import com.markin.mechatech.world.ModWorldGen;

public class MechaTech implements ModInitializer {
	public static final String MOD_ID = "mechatech";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModItems.initialize();
		ModRecipes.initialize();
		ModMenus.initialize();
		ModCreativeTab.initialize();
		ModWorldGen.initialize();

		LOGGER.info("Mecha-Tech carregado!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
