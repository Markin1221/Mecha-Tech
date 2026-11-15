package com.markin.mechatech.mecha;

/** Tipos de peça de mecha (cada um vira um item diferente). */
public enum PartType {
	HEAD("head"), TORSO("torso"), ARM("arm"), LEGS("legs"), BACKPACK("backpack");

	public final String id;

	PartType(String id) {
		this.id = id;
	}
}
