package com.markin.mechatech.items;

import com.markin.mechatech.MechaTech;
import com.markin.mechatech.block.ModBlocks;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTab {

	public static final ResourceKey<CreativeModeTab> MECHA_TECH_TAB_KEY =
		ResourceKey.create(Registries.CREATIVE_MODE_TAB, MechaTech.id("mecha_tech"));

	public static final CreativeModeTab MECHA_TECH_TAB = FabricCreativeModeTab.builder()
		.icon(() -> new ItemStack(ModItems.MINER_ARM))
		.title(Component.translatable("itemGroup.mechatech.mecha_tech"))
		.displayItems((params, output) -> {
				// Ferramentas
				output.accept(ModBlocks.MECHANICAL_WORKBENCH);
				output.accept(ModBlocks.COMBUSTION_GENERATOR);
				output.accept(ModBlocks.ALLOY_SMELTER);
				output.accept(ModBlocks.ENERGY_CABLE);
				output.accept(ModItems.MINER_ARM);
				output.accept(ModItems.DRILL);
				output.accept(ModItems.CHAINSAW);
				output.accept(ModItems.WRENCH);

				// Exo-armadura
				output.accept(ModBlocks.CALIBRATION_CABIN);
				output.accept(ModItems.EXO_FRAME);
				output.accept(ModItems.EXO_HELMET);
				output.accept(ModItems.EXO_CHESTPLATE);
				output.accept(ModItems.EXO_LEGGINGS);
				output.accept(ModItems.EXO_BOOTS);
				output.accept(ModItems.MODULE_NIGHT_VISION);
				output.accept(ModItems.MODULE_WATER_BREATHING);
				output.accept(ModItems.MODULE_STRENGTH);
				output.accept(ModItems.MODULE_RESISTANCE);
				output.accept(ModItems.MODULE_SPEED);
				output.accept(ModItems.MODULE_STEP_ASSIST);
				output.accept(ModItems.MODULE_JUMP_BOOST);
				output.accept(ModItems.MODULE_FALL_DAMPING);

				// Forja de Peças
				output.accept(ModBlocks.PART_FORGE);
				output.accept(ModBlocks.MECHA_ASSEMBLER_3);
				output.accept(ModBlocks.MECHA_ASSEMBLER_4);
				output.accept(ModBlocks.MECHA_ASSEMBLER_5);
				for (com.markin.mechatech.mecha.PartType t : com.markin.mechatech.mecha.PartType.values()) {
					for (com.markin.mechatech.mecha.PartVariant v : com.markin.mechatech.mecha.MechaParts.of(t)) {
						output.accept(com.markin.mechatech.block.entity.PartForgeBlockEntity.createPart(v, 100));
					}
				}

				// Componentes
				output.accept(ModItems.TITANIUM_STEEL_INGOT);
				output.accept(ModItems.CONDUCTIVE_COPPER_INGOT);
				output.accept(ModItems.TITANIUM_PLATE);
				output.accept(ModItems.NEODYMIUM_MAGNET);
				output.accept(ModItems.COATED_COPPER_WIRE);
				output.accept(ModItems.BASIC_CIRCUIT);
				output.accept(ModItems.SERVO_MOTOR);

				// Materiais
				output.accept(ModItems.RAW_TITANIUM);
				output.accept(ModItems.TITANIUM_INGOT);
				output.accept(ModItems.RAW_NEODYMIUM);
				output.accept(ModItems.NEODYMIUM_INGOT);
				output.accept(ModItems.RAW_RUBIC_COBALT);
				output.accept(ModItems.RUBIC_COBALT_INGOT);
				output.accept(ModItems.ETHER_CRYSTAL);

				// Minérios e blocos
				output.accept(ModBlocks.TITANIUM_ORE);
				output.accept(ModBlocks.DEEPSLATE_TITANIUM_ORE);
				output.accept(ModBlocks.NEODYMIUM_ORE);
				output.accept(ModBlocks.DEEPSLATE_NEODYMIUM_ORE);
				output.accept(ModBlocks.RUBIC_COBALT_ORE);
				output.accept(ModBlocks.ETHER_CRYSTAL_ORE);
				output.accept(ModBlocks.RAW_TITANIUM_BLOCK);
				output.accept(ModBlocks.RAW_NEODYMIUM_BLOCK);
				output.accept(ModBlocks.RAW_RUBIC_COBALT_BLOCK);
				output.accept(ModBlocks.TITANIUM_BLOCK);
				output.accept(ModBlocks.NEODYMIUM_BLOCK);
				output.accept(ModBlocks.RUBIC_COBALT_BLOCK);
				output.accept(ModBlocks.ETHER_CRYSTAL_BLOCK);
			})
		.build();

	public static void initialize() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MECHA_TECH_TAB_KEY, MECHA_TECH_TAB);
	}
}
