package com.markin.mechatech.mecha;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Dados guardados no item-peça: qual variante e a escala (80% a 130%). */
public record MechaPartData(String variant, int scale) {
	public static final int MIN_SCALE = 80, MAX_SCALE = 130, STEP = 10;

	public static final Codec<MechaPartData> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.fieldOf("variant").forGetter(MechaPartData::variant),
		Codec.INT.optionalFieldOf("scale", 100).forGetter(MechaPartData::scale)
	).apply(i, MechaPartData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, MechaPartData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, MechaPartData::variant,
		ByteBufCodecs.VAR_INT, MechaPartData::scale,
		MechaPartData::new);

	public PartVariant def() {
		return MechaParts.byId(variant);
	}

	private float s() { return scale / 100f; }

	// Peça maior = mais pesada, mais forte e blindada, mas mais lenta (e vice-versa)
	public int weight() { PartVariant v = def(); return v == null ? 0 : Math.round(v.weight() * s() * s()); }
	public int armor() { PartVariant v = def(); return v == null ? 0 : Math.round(v.armor() * s()); }
	public int power() { PartVariant v = def(); return v == null ? 0 : Math.round(v.power() * s()); }
	public int speed() { PartVariant v = def(); return v == null ? 0 : Math.max(0, Math.round(v.speed() * (2f - s()))); }
	public int energy() { PartVariant v = def(); return v == null ? 0 : Math.round(v.energy() * s()); }
	public int capacity() { PartVariant v = def(); return v == null ? 0 : Math.round(v.capacity() * s()); }
}
