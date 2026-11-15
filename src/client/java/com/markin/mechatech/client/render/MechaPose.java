package com.markin.mechatech.client.render;

import java.util.List;

import com.markin.mechatech.block.entity.PartForgeBlockEntity;
import com.markin.mechatech.mecha.MechaData;
import com.markin.mechatech.mecha.MechaLayout;
import com.markin.mechatech.mecha.MechaPartData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;

/**
 * Monta e anima o mecha com os modelos 3D das peças.
 * Origem = pés, frente = -Z. Índices: 0 cabeça, 1 torso, 2 braço E, 3 braço D, 4 pernas, 5 mochila.
 */
public final class MechaPose {
	/** Ordem em que a Montadora encaixa as peças. */
	public static final int[] BUILD_ORDER = {4, 1, 2, 3, 0, 5};

	public static final class Parts {
		public final ItemStackRenderState[] states = new ItemStackRenderState[6];
		public final ItemStackRenderState hip = new ItemStackRenderState(), legL = new ItemStackRenderState(), legR = new ItemStackRenderState();
		public final boolean[] present = new boolean[6];
		public MechaLayout layout = new MechaLayout(MechaData.EMPTY);
		public boolean split, treads;
		public String armL = "", armR = "";
		public Parts() {
			for (int i = 0; i < 6; i++) states[i] = new ItemStackRenderState();
		}
	}

	/** Parâmetros de animação (todos já interpolados). */
	public static final class Anim {
		public float walkPos, walkSpeed, air, land, age, attack, action, headYaw, headPitch;
		public boolean hideHead, shield;
	}

	public static final Anim STILL = new Anim();

	private MechaPose() { }

	public static void update(ItemModelResolver resolver, Parts p, MechaData data, Level level) {
		List<MechaPartData> list = data.parts();
		for (int i = 0; i < 6; i++) {
			MechaPartData d = list.get(i);
			p.present[i] = MechaData.present(d);
			if (p.present[i]) resolver.updateForTopItem(p.states[i], PartForgeBlockEntity.createPart(d.def(), d.scale()), ItemDisplayContext.NONE, level, null, i);
			else p.states[i].clear();
		}
		p.layout = new MechaLayout(data);
		p.armL = data.armLeft().variant();
		p.armR = data.armRight().variant();
		MechaPartData legs = data.legs();
		p.treads = legs.variant().equals("tread_legs");
		p.split = p.present[4];
		if (p.split) {
			ItemStack base = PartForgeBlockEntity.createPart(legs.def(), legs.scale());
			resolver.updateForTopItem(p.hip, variant(base, legs.variant() + "_hip"), ItemDisplayContext.NONE, level, null, 10);
			resolver.updateForTopItem(p.legL, variant(base, legs.variant() + "_l"), ItemDisplayContext.NONE, level, null, 11);
			resolver.updateForTopItem(p.legR, variant(base, legs.variant() + "_r"), ItemDisplayContext.NONE, level, null, 12);
		}
	}

	private static ItemStack variant(ItemStack base, String id) {
		ItemStack s = base.copy();
		s.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(id), List.of()));
		return s;
	}

	/**
	 * @param shown      quantas peças (na ordem de montagem) já estão encaixadas; 6 = todas
	 * @param installing progresso (0..1) da peça sendo encaixada agora (-1 = nenhuma)
	 * @param drop       altura de onde a peça desce durante a montagem
	 */
	public static void submit(Parts p, PoseStack pose, SubmitNodeCollector c, int light, Anim a, int shown, float installing, float drop) {
		MechaLayout l = p.layout;
		float ph = a.walkPos, sp = a.walkSpeed;
		float breathe = Mth.sin(a.age * 0.07f) * 0.012f * l.torso;
		// Pernas: passada alternada, encolhe no ar
		float legSwing = p.treads ? 0 : Mth.sin(ph) * 30f * sp;
		float tuck = a.air * 18f;
		// Corpo: sobe e desce 2x por passada, balança de lado, inclina pra frente andando
		float bob = (p.treads ? 0.015f : 0.07f) * l.legs * sp * Math.abs(Mth.cos(ph)) - a.land * 0.18f * l.legs;
		float roll = Mth.sin(ph) * 2.5f * sp;
		float twist = Mth.sin(ph) * 5f * sp;
		float lean = 7f * sp + a.air * 4f;
		float hipY = l.legs * 0.78f;

		for (int k = 0; k < 6; k++) {
			int i = BUILD_ORDER[k];
			if (!p.present[i] || p.states[i].isEmpty()) continue;
			float t;
			if (k < shown) t = 1f;
			else if (k == shown && installing >= 0) t = installing;
			else continue;
			if (i == 0 && a.hideHead) continue;
			float e = 1f - (1f - t) * (1f - t) * (1f - t);
			float grow = 0.6f + 0.4f * e, fall = (1f - e) * drop, spin = (1f - e) * 270f;

			pose.pushPose();
			pose.translate(0, fall, 0);
			if (t < 1f) pose.mulPose(Axis.YP.rotationDegrees(spin));

			if (i == 4) {
				legs(p, pose, c, light, l, hipY, bob, roll, legSwing, tuck, grow);
				pose.popPose();
				continue;
			}
			// ===== corpo (tudo acima da cintura) =====
			pose.translate(0, hipY + bob + breathe, 0);
			pose.mulPose(Axis.ZP.rotationDegrees(roll));
			pose.mulPose(Axis.YP.rotationDegrees(twist));
			pose.mulPose(Axis.XP.rotationDegrees(-lean));
			pose.translate(0, -hipY, 0);

			switch (i) {
				case 1 -> part(p.states[1], pose, c, light, 0, l.torsoBottom + l.torso * 0.5f, 0, l.torso * grow);
				case 0 -> {
					pose.translate(0, l.headY - l.head * 0.3f, 0);
					pose.mulPose(Axis.YP.rotationDegrees(-a.headYaw));
					pose.mulPose(Axis.XP.rotationDegrees(-a.headPitch));
					part(p.states[0], pose, c, light, 0, l.head * 0.3f, 0, l.head * grow);
				}
				case 2, 3 -> {
					boolean left = i == 2;
					float size = left ? l.armL : l.armR;
					float x = left ? l.armX_L : l.armX_R, y = left ? l.armY_L : l.armY_R;
					String v = left ? p.armL : p.armR;
					// braço balança ao contrário da perna do mesmo lado
					float swing = (left ? -1 : 1) * Mth.sin(ph) * 22f * sp;
					float out = 4f + a.air * 14f;
					float raise = 0;
					if (!left) raise += Mth.sin(Mth.sqrt(a.attack) * Mth.PI) * 75f;          // soco/golpe
					if (isActive(v) && a.action > 0) raise += Mth.sin(a.action * Mth.PI) * 80f; // habilidade
					if (v.equals("shield_arm") && a.shield) raise = Math.max(raise, 70f);
					if (v.equals("cannon_arm")) raise = Math.max(raise, 20f + (float) Math.sin(a.age * 0.05) * 2f);
					pose.translate(x, y + size * 0.375f, 0);
					pose.mulPose(Axis.XP.rotationDegrees(swing + raise));
					pose.mulPose(Axis.ZP.rotationDegrees(left ? -out : out));
					part(p.states[i], pose, c, light, 0, -size * 0.375f, 0, size * grow);
				}
				case 5 -> {
					pose.translate(0, l.torsoBottom + l.torso * 0.5f, l.packZ);
					pose.mulPose(Axis.YP.rotationDegrees(180f));
					part(p.states[5], pose, c, light, 0, 0, 0, l.pack * grow);
				}
				default -> { }
			}
			pose.popPose();
		}
	}

	private static boolean isActive(String v) {
		return v.equals("cannon_arm") || v.equals("claw_arm") || v.equals("shield_arm") || v.equals("fist_arm");
	}

	private static void legs(Parts p, PoseStack pose, SubmitNodeCollector c, int light, MechaLayout l,
							 float hipY, float bob, float roll, float swing, float tuck, float grow) {
		float size = l.legs * grow;
		if (!p.split || p.hip.isEmpty()) {
			part(p.states[4], pose, c, light, 0, l.legs * 0.5f + bob * 0.5f, 0, size);
			return;
		}
		pose.pushPose();
		pose.translate(0, hipY + bob, 0);
		pose.mulPose(Axis.ZP.rotationDegrees(roll * 0.5f));
		part(p.hip, pose, c, light, 0, l.legs * 0.5f - hipY, 0, size);
		for (int side = 0; side < 2; side++) {
			float ang = (side == 0 ? swing : -swing) + (side == 0 ? tuck : -tuck * 0.4f);
			// a perna que vai pra trás estica pra compensar a subida do corpo
			pose.pushPose();
			pose.mulPose(Axis.XP.rotationDegrees(ang));
			part(side == 0 ? p.legL : p.legR, pose, c, light, 0, l.legs * 0.5f - hipY - bob, 0, size);
			pose.popPose();
		}
		pose.popPose();
	}

	private static void part(ItemStackRenderState st, PoseStack pose, SubmitNodeCollector c, int light, float x, float y, float z, float s) {
		if (st.isEmpty()) return;
		pose.pushPose();
		pose.translate(x, y, z);
		pose.scale(s, s, s);
		st.submit(pose, c, light, OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}
}
