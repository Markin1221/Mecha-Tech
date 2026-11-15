package com.markin.mechatech.mecha;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Um mecha completo: 6 encaixes (cabeça, torso, braço esq., braço dir., pernas, mochila) + energia guardada.
 * Encaixe vazio = variante "".
 */
public record MechaData(MechaPartData head, MechaPartData torso, MechaPartData armLeft, MechaPartData armRight,
						MechaPartData legs, MechaPartData backpack, int energy) {
	public static final MechaPartData NONE = new MechaPartData("", 100);
	public static final MechaData EMPTY = new MechaData(NONE, NONE, NONE, NONE, NONE, NONE, 0);

	/** Altura em blocos de um mecha 100%. */
	public static final float BASE_HEIGHT = 3.0f;

	private static final Codec<MechaPartData> P = MechaPartData.CODEC;
	public static final Codec<MechaData> CODEC = RecordCodecBuilder.create(i -> i.group(
		P.optionalFieldOf("head", NONE).forGetter(MechaData::head),
		P.optionalFieldOf("torso", NONE).forGetter(MechaData::torso),
		P.optionalFieldOf("arm_left", NONE).forGetter(MechaData::armLeft),
		P.optionalFieldOf("arm_right", NONE).forGetter(MechaData::armRight),
		P.optionalFieldOf("legs", NONE).forGetter(MechaData::legs),
		P.optionalFieldOf("backpack", NONE).forGetter(MechaData::backpack),
		Codec.INT.optionalFieldOf("energy", 0).forGetter(MechaData::energy)
	).apply(i, MechaData::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, MechaData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

	public List<MechaPartData> parts() {
		return List.of(head, torso, armLeft, armRight, legs, backpack);
	}

	public static boolean present(MechaPartData p) {
		return p != null && p.def() != null;
	}

	public MechaData withEnergy(int e) {
		return new MechaData(head, torso, armLeft, armRight, legs, backpack, Math.max(0, Math.min(e, maxEnergy())));
	}

	public boolean complete() {
		return present(head) && present(torso) && present(armLeft) && present(armRight) && present(legs);
	}

	// ===== Atributos somados =====
	private int sum(java.util.function.ToIntFunction<MechaPartData> f) {
		int s = 0;
		for (MechaPartData p : parts()) if (present(p)) s += f.applyAsInt(p);
		return s;
	}

	public int weight() { return sum(MechaPartData::weight); }
	public int armor() { return sum(MechaPartData::armor); }
	public int power() { return sum(MechaPartData::power); }
	/** Consumo em EM por tick andando. */
	public int usage() { return 2 + sum(MechaPartData::energy); }
	public int capacity() { return present(torso) ? torso.capacity() : 0; }
	public boolean overweight() { return weight() > capacity(); }

	/** Velocidade (0..~12): pernas + jato, menos o peso extra. */
	public int speed() {
		return Math.max(1, sum(MechaPartData::speed) - Math.max(0, (weight() - 80) / 25));
	}

	/** Maior escala entre as peças: define o tamanho do mecha e qual Montadora precisa. */
	public int scale() {
		int m = 0;
		for (MechaPartData p : parts()) if (present(p)) m = Math.max(m, p.scale());
		return m == 0 ? 100 : m;
	}

	public float height() {
		float legs = present(this.legs) ? this.legs.scale() : 100, t = present(torso) ? torso.scale() : 100, h = present(head) ? head.scale() : 100;
		return BASE_HEIGHT * (legs * 0.36f + t * 0.40f + h * 0.24f) / 100f;
	}

	public int maxEnergy() {
		int base = 40000;
		if (present(backpack) && backpack.variant().equals("battery_backpack")) base += 80000 * backpack.scale() / 100;
		return base;
	}

	public int maxHealth() { return 40 + armor() * 2; }

	public boolean has(String variant) {
		for (MechaPartData p : parts()) if (p.variant().equals(variant)) return true;
		return false;
	}

	public float jump() {
		if (!present(legs)) return 0.5f;
		return switch (legs.variant()) {
			case "jumper_legs" -> 1.05f;
			case "tread_legs" -> 0.42f;
			default -> 0.62f;
		};
	}

	// ===== Texto compacto pra sincronizar com o cliente (entidade) =====
	public String encode() {
		StringBuilder b = new StringBuilder();
		for (MechaPartData p : parts()) b.append(p.variant()).append(':').append(p.scale()).append(';');
		return b.toString();
	}

	public static MechaData decode(String s, int energy) {
		MechaPartData[] a = {NONE, NONE, NONE, NONE, NONE, NONE};
		String[] parts = s.split(";");
		for (int i = 0; i < parts.length && i < 6; i++) {
			int c = parts[i].indexOf(':');
			if (c <= 0) continue;
			try {
				a[i] = new MechaPartData(parts[i].substring(0, c), Integer.parseInt(parts[i].substring(c + 1)));
			} catch (NumberFormatException ignored) { }
		}
		return new MechaData(a[0], a[1], a[2], a[3], a[4], a[5], energy);
	}
}
