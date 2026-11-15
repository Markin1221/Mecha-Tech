package com.markin.mechatech.client.render;

import com.markin.mechatech.block.entity.PartForgeBlockEntity;
import com.markin.mechatech.mecha.MechaData;
import com.markin.mechatech.mecha.MechaPartData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;

/**
 * Monta o visual do mecha com os modelos 3D das peças (os mesmos dos itens).
 * Origem = pés do mecha, frente = -Z. Ordem dos índices: cabeça, torso, braço E, braço D, pernas, mochila.
 */
public final class MechaPose {
	/** Ordem em que a Montadora encaixa as peças. */
	public static final int[] BUILD_ORDER = {4, 1, 2, 3, 0, 5};
	private static final float UNIT = MechaData.BASE_HEIGHT / 2.6f;

	public static final class Parts {
		public final ItemStackRenderState[] states = new ItemStackRenderState[6];
		public final float[] scale = new float[6];
		public final boolean[] present = new boolean[6];
		public Parts() {
			for (int i = 0; i < 6; i++) states[i] = new ItemStackRenderState();
		}
	}

	private MechaPose() { }

	public static void update(ItemModelResolver resolver, Parts parts, MechaData data, Level level) {
		var list = data.parts();
		for (int i = 0; i < 6; i++) {
			MechaPartData p = list.get(i);
			parts.present[i] = MechaData.present(p);
			parts.scale[i] = p.scale() / 100f;
			if (parts.present[i]) {
				resolver.updateForTopItem(parts.states[i], PartForgeBlockEntity.createPart(p.def(), p.scale()), ItemDisplayContext.NONE, level, null, i);
			} else {
				parts.states[i].clear();
			}
		}
	}

	/**
	 * @param shown     quantas peças (na ordem de montagem) já estão encaixadas; 6 = todas
	 * @param installing progresso (0..1) da peça sendo encaixada agora (-1 = nenhuma)
	 * @param drop       altura de onde a peça desce durante a montagem
	 */
	public static void submit(Parts p, PoseStack pose, SubmitNodeCollector c, int light, float walkPos, float walkSpeed,
							  int shown, float installing, float drop) {
		float sL = p.present[4] ? p.scale[4] : 1f, sT = p.present[1] ? p.scale[1] : 1f;
		float sH = p.scale[0], sA = p.scale[2], sA2 = p.scale[3], sB = p.scale[5];
		float legs = UNIT * sL, torso = UNIT * sT;
		float bob = Math.abs(Mth.sin(walkPos * 0.6f)) * 0.06f * walkSpeed * UNIT;
		float swing = Mth.sin(walkPos * 0.6f) * 28f * Math.min(1f, walkSpeed);

		float torsoBottom = legs * 0.9f;
		float torsoTop = torsoBottom + torso;
		float[][] at = new float[6][];
		float head = UNIT * 0.8f * sH;
		at[0] = new float[]{0, torsoTop - torso * 0.08f + head * 0.5f, 0, head};
		at[1] = new float[]{0, torsoBottom + torso * 0.5f, 0, torso};
		float aL = UNIT * sA, aR = UNIT * sA2;
		at[2] = new float[]{-(0.33f * torso + 0.25f * aL), torsoTop - torso * 0.05f - aL * 0.5f, 0, aL};
		at[3] = new float[]{0.33f * torso + 0.25f * aR, torsoTop - torso * 0.05f - aR * 0.5f, 0, aR};
		at[4] = new float[]{0, legs * 0.5f, 0, legs};
		float bp = UNIT * 0.9f * sB;
		at[5] = new float[]{0, torsoBottom + torso * 0.5f, 0.3f * torso + 0.3f * bp, bp};

		for (int k = 0; k < 6; k++) {
			int i = BUILD_ORDER[k];
			if (!p.present[i] || p.states[i].isEmpty()) continue;
			float t;
			if (k < shown) t = 1f;
			else if (k == shown && installing >= 0) t = installing;
			else continue;

			float[] a = at[i];
			float e = 1f - (1f - t) * (1f - t) * (1f - t);
			pose.pushPose();
			float y = a[1] + (i == 4 ? 0 : bob) + (1f - e) * drop;
			pose.translate(a[0], y, a[2]);
			if (t < 1f) pose.mulPose(Axis.YP.rotationDegrees((1f - e) * 270f));
			if (i == 2 || i == 3) {
				// balanço do braço a partir do ombro
				pose.translate(0, a[3] * 0.5f, 0);
				pose.mulPose(Axis.XP.rotationDegrees(i == 2 ? swing : -swing));
				pose.translate(0, -a[3] * 0.5f, 0);
			}
			if (i == 5) pose.mulPose(Axis.YP.rotationDegrees(180f));
			float s = a[3] * (0.6f + 0.4f * e);
			pose.scale(s, s, s);
			p.states[i].submit(pose, c, light, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}
	}
}
