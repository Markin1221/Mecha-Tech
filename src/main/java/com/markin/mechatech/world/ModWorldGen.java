package com.markin.mechatech.world;

import com.markin.mechatech.MechaTech;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ModWorldGen {

	public static final ResourceKey<PlacedFeature> ORE_TITANIUM = key("ore_titanium");
	public static final ResourceKey<PlacedFeature> ORE_NEODYMIUM = key("ore_neodymium");
	public static final ResourceKey<PlacedFeature> ORE_RUBIC_COBALT = key("ore_rubic_cobalt");
	public static final ResourceKey<PlacedFeature> ORE_ETHER_CRYSTAL = key("ore_ether_crystal");

	private static ResourceKey<PlacedFeature> key(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, MechaTech.id(name));
	}

	public static void initialize() {
		GenerationStep.Decoration ores = GenerationStep.Decoration.UNDERGROUND_ORES;

		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), ores, ORE_TITANIUM);
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), ores, ORE_NEODYMIUM);
		BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), ores, ORE_RUBIC_COBALT);
		// Só nas ilhas externas do End (não na ilha do dragão)
		BiomeModifications.addFeature(
			BiomeSelectors.includeByKey(Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS, Biomes.END_BARRENS, Biomes.SMALL_END_ISLANDS),
			ores, ORE_ETHER_CRYSTAL);
	}
}
