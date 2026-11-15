package com.markin.mechatech.component;

import com.markin.mechatech.MechaTech;
import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.ItemContainerContents;

public class ModComponents {

	/** Módulos instalados numa peça da exo-armadura (2 por peça). */
	public static final DataComponentType<ItemContainerContents> EXO_MODULES = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE, MechaTech.id("exo_modules"),
		DataComponentType.<ItemContainerContents>builder()
			.persistent(ItemContainerContents.CODEC)
			.networkSynchronized(ItemContainerContents.STREAM_CODEC)
			.build());

	/** Energia guardada no peitoral (núcleo) da exo-armadura. */
	public static final DataComponentType<Integer> EXO_ENERGY = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE, MechaTech.id("exo_energy"),
		DataComponentType.<Integer>builder()
			.persistent(Codec.INT)
			.networkSynchronized(ByteBufCodecs.VAR_INT)
			.build());

	/** Peça de mecha: variante + escala. */
	public static final DataComponentType<com.markin.mechatech.mecha.MechaPartData> MECHA_PART = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE, MechaTech.id("mecha_part"),
		DataComponentType.<com.markin.mechatech.mecha.MechaPartData>builder()
			.persistent(com.markin.mechatech.mecha.MechaPartData.CODEC)
			.networkSynchronized(com.markin.mechatech.mecha.MechaPartData.STREAM_CODEC)
			.build());

	/** Mecha completo empacotado (peças + energia). */
	public static final DataComponentType<com.markin.mechatech.mecha.MechaData> MECHA_DATA = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE, MechaTech.id("mecha_data"),
		DataComponentType.<com.markin.mechatech.mecha.MechaData>builder()
			.persistent(com.markin.mechatech.mecha.MechaData.CODEC)
			.networkSynchronized(com.markin.mechatech.mecha.MechaData.STREAM_CODEC)
			.build());

	public static void initialize() {
		// Carrega a classe.
	}
}
