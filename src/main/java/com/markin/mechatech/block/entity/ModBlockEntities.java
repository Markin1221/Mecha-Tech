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

	public static final BlockEntityType<CalibrationCabinBlockEntity> CALIBRATION_CABIN = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE, MechaTech.id("calibration_cabin"),
		FabricBlockEntityTypeBuilder.create(CalibrationCabinBlockEntity::new, ModBlocks.CALIBRATION_CABIN).build());

	public static final BlockEntityType<PartForgeBlockEntity> PART_FORGE = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE, MechaTech.id("part_forge"),
		FabricBlockEntityTypeBuilder.create(PartForgeBlockEntity::new, ModBlocks.PART_FORGE).build());

	public static final BlockEntityType<MechaAssemblerBlockEntity> MECHA_ASSEMBLER = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE, MechaTech.id("mecha_assembler"),
		FabricBlockEntityTypeBuilder.create(MechaAssemblerBlockEntity::new,
			ModBlocks.MECHA_ASSEMBLER_3, ModBlocks.MECHA_ASSEMBLER_4, ModBlocks.MECHA_ASSEMBLER_5).build());

	public static void initialize() {
		// Expõe a energia das máquinas pra outros mods (Team Reborn Energy)
		EnergyStorage.SIDED.registerForBlockEntity((be, side) -> be.energy, COMBUSTION_GENERATOR);
		EnergyStorage.SIDED.registerForBlockEntity((be, side) -> be.energy, ALLOY_SMELTER);
		EnergyStorage.SIDED.registerForBlockEntity((be, side) -> be.energy, ENERGY_CABLE);
		EnergyStorage.SIDED.registerForBlockEntity((be, side) -> be.energy, CALIBRATION_CABIN);
		// Forja: qualquer um dos 8 blocos recebe energia (vai pra parte principal)
		EnergyStorage.SIDED.registerForBlocks((world, pos, state, be, side) ->
			world.getBlockEntity(com.markin.mechatech.block.PartForgeBlock.mainPos(state, pos)) instanceof PartForgeBlockEntity f ? f.energy : null,
			ModBlocks.PART_FORGE);
		// Montadora: qualquer bloco recebe energia (vai pra parte principal)
		EnergyStorage.SIDED.registerForBlocks((world, pos, state, be, side) ->
			state.getBlock() instanceof com.markin.mechatech.block.MechaAssemblerBlock b
				&& world.getBlockEntity(b.mainPos(state, pos)) instanceof MechaAssemblerBlockEntity m ? m.energy : null,
			ModBlocks.MECHA_ASSEMBLER_3, ModBlocks.MECHA_ASSEMBLER_4, ModBlocks.MECHA_ASSEMBLER_5);
	}
}
