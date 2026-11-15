package com.markin.mechatech.entity;

import com.markin.mechatech.MechaTech;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
	private static final ResourceKey<EntityType<?>> MECHA_KEY = ResourceKey.create(Registries.ENTITY_TYPE, MechaTech.id("mecha"));

	public static final EntityType<MechaEntity> MECHA = Registry.register(BuiltInRegistries.ENTITY_TYPE, MECHA_KEY,
		EntityType.Builder.<MechaEntity>of(MechaEntity::new, MobCategory.MISC)
			.sized(1.4f, 3.0f).clientTrackingRange(10).build(MECHA_KEY));

	public static void initialize() {
		FabricDefaultAttributeRegistry.register(MECHA, MechaEntity.createAttributes());
	}
}
