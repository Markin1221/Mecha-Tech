package com.markin.mechatech.items;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

import com.markin.mechatech.MechaTech;
import com.markin.mechatech.exo.ExoArmorItem;
import com.markin.mechatech.exo.ExoModule;
import com.markin.mechatech.exo.ExoModuleItem;
import com.markin.mechatech.mecha.MechaPartItem;
import com.markin.mechatech.mecha.PartType;

import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.component.TooltipDisplay;
import com.markin.mechatech.component.ModComponents;

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

	// ===== Exo-armadura (Tier 1) =====
	public static final Item EXO_FRAME = register("exo_frame", Item::new, new Item.Properties().stacksTo(1));

	public static final Item EXO_HELMET = register("exo_helmet", p -> new ExoArmorItem(ArmorType.HELMET, p),
		exo(ArmorType.HELMET));

	public static final Item EXO_CHESTPLATE = register("exo_chestplate", p -> new ExoArmorItem(ArmorType.CHESTPLATE, p),
		exo(ArmorType.CHESTPLATE));

	public static final Item EXO_LEGGINGS = register("exo_leggings", p -> new ExoArmorItem(ArmorType.LEGGINGS, p),
		exo(ArmorType.LEGGINGS));

	public static final Item EXO_BOOTS = register("exo_boots", p -> new ExoArmorItem(ArmorType.BOOTS, p),
		exo(ArmorType.BOOTS));

	/** Exo: sem durabilidade (usa energia), "Inquebrável" escondido na descrição. */
	private static Item.Properties exo(ArmorType type) {
		return new Item.Properties().humanoidArmor(ModArmorMaterials.EXO, type)
			.component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
			.component(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.UNBREAKABLE, true))
			.component(ModComponents.EXO_ENERGY, 0);
	}

	// Módulos
	public static final Item MODULE_NIGHT_VISION = module(ExoModule.NIGHT_VISION);
	public static final Item MODULE_WATER_BREATHING = module(ExoModule.WATER_BREATHING);
	public static final Item MODULE_STRENGTH = module(ExoModule.STRENGTH);
	public static final Item MODULE_RESISTANCE = module(ExoModule.RESISTANCE);
	public static final Item MODULE_SPEED = module(ExoModule.SPEED);
	public static final Item MODULE_STEP_ASSIST = module(ExoModule.STEP_ASSIST);
	public static final Item MODULE_JUMP_BOOST = module(ExoModule.JUMP_BOOST);
	public static final Item MODULE_FALL_DAMPING = module(ExoModule.FALL_DAMPING);

	private static Item module(ExoModule m) {
		return register("module_" + m.id, p -> new ExoModuleItem(m, p), new Item.Properties());
	}

	// ===== Peças de mecha (Forja de Peças) =====
	public static final Item MECHA_HEAD = register("mecha_head", p -> new MechaPartItem(PartType.HEAD, p), new Item.Properties());
	public static final Item MECHA_TORSO = register("mecha_torso", p -> new MechaPartItem(PartType.TORSO, p), new Item.Properties());
	public static final Item MECHA_ARM = register("mecha_arm", p -> new MechaPartItem(PartType.ARM, p), new Item.Properties());
	public static final Item MECHA_LEGS = register("mecha_legs", p -> new MechaPartItem(PartType.LEGS, p), new Item.Properties());
	public static final Item MECHA_BACKPACK = register("mecha_backpack", p -> new MechaPartItem(PartType.BACKPACK, p), new Item.Properties());

	// ===== Ferramentas =====
	public static final Item DRILL = register("drill", Item::new,
		new Item.Properties().tool(ModToolMaterials.TITANIUM, ModToolMaterials.MINEABLE_WITH_DRILL, 2.0F, -2.8F, 0.0F));

	public static final Item CHAINSAW = register("chainsaw", Item::new,
		new Item.Properties().tool(ModToolMaterials.TITANIUM, ModToolMaterials.MINEABLE_WITH_CHAINSAW, 6.0F, -3.0F, 5.0F));

	public static final Item MECHA = register("mecha", MechaItem::new, new Item.Properties().stacksTo(1));

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
