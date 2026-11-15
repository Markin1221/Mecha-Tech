package com.markin.mechatech.block.entity;

import com.markin.mechatech.MechaTech;
import com.markin.mechatech.block.ModBlocks;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import team.reborn.energy.api.EnergyStorage;

public class ModBlockEntities {

	public static final BlockEntityType<CombustionGeneratorBlockEntity> COMBUSTION_GENERATOR = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE, MechaTech.id("combustion_generator"),
		FabricBlockEntityTypeBuilder.create(CombustionGeneratorBlockEntity::new, ModBlocks.COMBUSTION_GENERATOR).build());

	public static final BlockEntityType<AlloySmelterBlockEntity> ALLOY_SMELTER = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE, MechaTech.id("alloy_smelter"),
		FabricBlockEntityTypeBuilder.create(AlloySmelterBlockEntity::new, ModBlocks.ALLOY_SMELTER).build());

	public static final BlockEntityType<EnergyCableBlockEntity> ENERGY_CABLE = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE, MechaTech.id("energy_cable"),
		FabricBlockEntityTypeBuilder.create(EnergyCableBlockEntity::new, ModBlocks.ENERGY_CABLE).build());

	public static void initialize() {
		// Expõe a energia das máquinas pra outros mods (Team Reborn Energy)
		EnergyStorage.SIDED.registerForBlockEntity((be, side) -> be.energy, COMBUSTION_GENERATOR);
		EnergyStorage.SIDED.registerForBlockEntity((be, side) -> be.energy, ALLOY_SMELTER);
		EnergyStorage.SIDED.registerForBlockEntity((be, side) -> be.energy, ENERGY_CABLE);
	}
}
