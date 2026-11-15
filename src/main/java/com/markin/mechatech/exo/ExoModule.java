package com.markin.mechatech.exo;

import net.minecraft.world.item.equipment.ArmorType;

/**
 * Módulos da exo-armadura: em qual peça vão e quanto gastam (EM por segundo).
 * Vários módulos só gastam quando estão sendo úteis (ex: respirador só debaixo d'água).
 */
public enum ExoModule {
	NIGHT_VISION("night_vision", ArmorType.HELMET, 1),       // só no escuro
	WATER_BREATHING("water_breathing", ArmorType.HELMET, 1), // só debaixo d'água
	STRENGTH("strength", ArmorType.CHESTPLATE, 3),
	RESISTANCE("resistance", ArmorType.CHESTPLATE, 3),
	SPEED("speed", ArmorType.LEGGINGS, 2),                    // só correndo
	STEP_ASSIST("step_assist", ArmorType.LEGGINGS, 1),
	JUMP_BOOST("jump_boost", ArmorType.BOOTS, 1),
	FALL_DAMPING("fall_damping", ArmorType.BOOTS, 1);         // só no ar

	public final String id;
	public final ArmorType piece;
	public final int costPerSecond;

	ExoModule(String id, ArmorType piece, int costPerSecond) {
		this.id = id;
		this.piece = piece;
		this.costPerSecond = costPerSecond;
	}
}
