package com.markin.mechatech.block;

import java.util.function.Function;

import com.markin.mechatech.MechaTech;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

	public static final Block TITANIUM_ORE = register("titanium_ore",
		p -> new DropExperienceBlock(UniformInt.of(1, 3), p),
		BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));

	public static final Block DEEPSLATE_TITANIUM_ORE = register("deepslate_titanium_ore",
		p -> new DropExperienceBlock(UniformInt.of(1, 3), p),
		BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));

	public static final Block NEODYMIUM_ORE = register("neodymium_ore",
		p -> new DropExperienceBlock(UniformInt.of(2, 4), p),
		BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));

	public static final Block DEEPSLATE_NEODYMIUM_ORE = register("deepslate_neodymium_ore",
		p -> new DropExperienceBlock(UniformInt.of(2, 4), p),
		BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));

	public static final Block RUBIC_COBALT_ORE = register("rubic_cobalt_ore",
		p -> new DropExperienceBlock(UniformInt.of(3, 6), p),
		BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_QUARTZ_ORE));

	public static final Block ETHER_CRYSTAL_ORE = register("ether_crystal_ore",
		p -> new DropExperienceBlock(UniformInt.of(4, 8), p),
		BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE).strength(3.5F, 9.0F).requiresCorrectToolForDrops().lightLevel(state -> 5));

	public static final Block TITANIUM_BLOCK = register("titanium_block", Block::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));

	public static final Block NEODYMIUM_BLOCK = register("neodymium_block", Block::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));

	public static final Block RUBIC_COBALT_BLOCK = register("rubic_cobalt_block", Block::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));

	public static final Block RAW_TITANIUM_BLOCK = register("raw_titanium_block", Block::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK));

	public static final Block RAW_NEODYMIUM_BLOCK = register("raw_neodymium_block", Block::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK));

	public static final Block RAW_RUBIC_COBALT_BLOCK = register("raw_rubic_cobalt_block", Block::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK));

	public static final Block ETHER_CRYSTAL_BLOCK = register("ether_crystal_block", Block::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).lightLevel(state -> 10));

	// ===== Máquinas =====
	public static final Block MECHANICAL_WORKBENCH = register("mechanical_workbench", MechanicalWorkbenchBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(3.5F, 6.0F));

	public static final Block COMBUSTION_GENERATOR = register("combustion_generator", CombustionGeneratorBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(3.5F, 6.0F)
			.lightLevel(state -> state.getValue(MachineBlock.LIT) ? 13 : 0));

	public static final Block ALLOY_SMELTER = register("alloy_smelter", AlloySmelterBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(3.5F, 6.0F)
			.lightLevel(state -> state.getValue(MachineBlock.LIT) ? 11 : 0));

	public static final Block ENERGY_CABLE = register("energy_cable", EnergyCableBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(0.6F).noOcclusion());

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, MechaTech.id(name));
		Block block = factory.apply(properties.setId(blockKey));

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, MechaTech.id(name));
		BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
		Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

		return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
	}

	public static void initialize() {
		// Carrega a classe e registra todos os blocos.
	}
}
