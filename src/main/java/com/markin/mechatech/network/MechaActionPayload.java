package com.markin.mechatech.network;

import com.markin.mechatech.MechaTech;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente → servidor: piloto apertou o botão de habilidade do mecha. */
public record MechaActionPayload(int action) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<MechaActionPayload> TYPE = new CustomPacketPayload.Type<>(MechaTech.id("mecha_action"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MechaActionPayload> CODEC =
		StreamCodec.composite(ByteBufCodecs.VAR_INT, MechaActionPayload::action, MechaActionPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
