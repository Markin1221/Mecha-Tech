package com.markin.mechatech.items;

import java.util.Map;

import com.markin.mechatech.MechaTech;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class ModArmorMaterials {
	public static final TagKey<Item> EXO_REPAIR = TagKey.create(Registries.ITEM, MechaTech.id("exo_repair_materials"));
	public static final ResourceKey<EquipmentAsset> EXO_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, MechaTech.id("exo"));

	/** Exo-armadura: entre ferro e diamante, com um pouco de resistência. */
	public static final ArmorMaterial EXO = new ArmorMaterial(
		30,
		Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 7, ArmorType.LEGGINGS, 6, ArmorType.BOOTS, 3, ArmorType.BODY, 7),
		12, SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 0.05F, EXO_REPAIR, EXO_ASSET);
}
