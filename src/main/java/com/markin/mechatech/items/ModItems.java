package com.markin.mechatech.items;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

import com.markin.mechatech.MechaTech;

public class ModItems {

	// ===== Materiais =====
	public static final Item RAW_TITANIUM = register("raw_titanium", Item::new, new Item.Properties());

	public static final Item TITANIUM_INGOT = register("titanium_ingot", Item::new, new Item.Properties());

	public static final Item RAW_NEODYMIUM = register("raw_neodymium", Item::new, new Item.Properties());

	public static final Item NEODYMIUM_INGOT = register("neodymium_ingot", Item::new, new Item.Properties());

	public static final Item RAW_RUBIC_COBALT = register("raw_rubic_cobalt", Item::new, new Item.Properties());

	public static final Item RUBIC_COBALT_INGOT = register("rubic_cobalt_ingot", Item::new, new Item.Properties());

	public static final Item ETHER_CRYSTAL = register("ether_crystal", Item::new, new Item.Properties());

	// ===== Ligas (Fundição de Ligas) =====
	public static final Item TITANIUM_STEEL_INGOT = register("titanium_steel_ingot", Item::new, new Item.Properties());

	public static final Item CONDUCTIVE_COPPER_INGOT = register("conductive_copper_ingot", Item::new, new Item.Properties());

	// ===== Componentes (Bancada Mecânica) =====
	public static final Item TITANIUM_PLATE = register("titanium_plate", Item::new, new Item.Properties());

	public static final Item NEODYMIUM_MAGNET = register("neodymium_magnet", Item::new, new Item.Properties());

	public static final Item COATED_COPPER_WIRE = register("coated_copper_wire", Item::new, new Item.Properties());

	public static final Item BASIC_CIRCUIT = register("basic_circuit", Item::new, new Item.Properties());

	public static final Item SERVO_MOTOR = register("servo_motor", Item::new, new Item.Properties());

	// ===== Ferramentas =====
	public static final Item DRILL = register("drill", Item::new,
		new Item.Properties().tool(ModToolMaterials.TITANIUM, ModToolMaterials.MINEABLE_WITH_DRILL, 2.0F, -2.8F, 0.0F));

	public static final Item CHAINSAW = register("chainsaw", Item::new,
		new Item.Properties().tool(ModToolMaterials.TITANIUM, ModToolMaterials.MINEABLE_WITH_CHAINSAW, 6.0F, -3.0F, 5.0F));

	public static final Item WRENCH = register("wrench", WrenchItem::new,
		new Item.Properties().durability(512).repairable(ModToolMaterials.TITANIUM_TOOL_MATERIALS));

	public static final Item MINER_ARM = register(
		"miner_arm",
		MinerArmItem::new,
		new Item.Properties()
			.pickaxe(ToolMaterial.DIAMOND, 1.0F, -2.8F)
			.stacksTo(1)
	);

	public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, MechaTech.id(name));
		T item = itemFactory.apply(settings.setId(itemKey));
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);
		return item;
	}

	public static void initialize() {
		// Carrega a classe e registra todos os itens.
	}
}
