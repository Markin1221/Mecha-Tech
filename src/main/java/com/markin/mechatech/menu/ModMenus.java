package com.markin.mechatech.menu;

import com.markin.mechatech.MechaTech;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenus {

	public static final MenuType<MechanicalWorkbenchMenu> MECHANICAL_WORKBENCH = Registry.register(
		BuiltInRegistries.MENU, MechaTech.id("mechanical_workbench"),
		new MenuType<>(MechanicalWorkbenchMenu::new, FeatureFlags.VANILLA_SET));

	public static final MenuType<CombustionGeneratorMenu> COMBUSTION_GENERATOR = Registry.register(
		BuiltInRegistries.MENU, MechaTech.id("combustion_generator"),
		new MenuType<>(CombustionGeneratorMenu::new, FeatureFlags.VANILLA_SET));

	public static final MenuType<AlloySmelterMenu> ALLOY_SMELTER = Registry.register(
		BuiltInRegistries.MENU, MechaTech.id("alloy_smelter"),
		new MenuType<>(AlloySmelterMenu::new, FeatureFlags.VANILLA_SET));

	public static final MenuType<CalibrationCabinMenu> CALIBRATION_CABIN = Registry.register(
		BuiltInRegistries.MENU, MechaTech.id("calibration_cabin"),
		new MenuType<>(CalibrationCabinMenu::new, FeatureFlags.VANILLA_SET));

	public static final MenuType<PartForgeMenu> PART_FORGE = Registry.register(
		BuiltInRegistries.MENU, MechaTech.id("part_forge"),
		new MenuType<>(PartForgeMenu::new, FeatureFlags.VANILLA_SET));

	public static final MenuType<MechaAssemblerMenu> MECHA_ASSEMBLER = Registry.register(
		BuiltInRegistries.MENU, MechaTech.id("mecha_assembler"),
		new MenuType<>(MechaAssemblerMenu::new, FeatureFlags.VANILLA_SET));

	public static void initialize() {
		// Carrega a classe e registra tudo.
	}
}
