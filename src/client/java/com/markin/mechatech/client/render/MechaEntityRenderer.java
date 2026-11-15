package com.markin.mechatech.client.render;

import com.markin.mechatech.entity.MechaEntity;
import com.markin.mechatech.mecha.MechaData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/** Desenha o mecha montando e animando os modelos 3D das peças. */
public class MechaEntityRenderer extends EntityRenderer<MechaEntity, MechaEntityRenderer.State> {
	private final ItemModelResolver items;

	public static class State extends EntityRenderState {
		final MechaPose.Parts parts = new MechaPose.Parts();
		final MechaPose.Anim anim = new MechaPose.Anim();
		float bodyRot, deathTime;
		boolean hurt;
	}

	public MechaEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.items = context.getItemModelResolver();
		this.shadowRadius = 1.1f;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(MechaEntity e, State s, float pt) {
		super.extractRenderState(e, s, pt);
		MechaData d = e.data();
		MechaPose.update(items, s.parts, d, e.level());
		s.bodyRot = Mth.rotLerp(pt, e.yBodyRotO, e.yBodyRot);
		s.deathTime = e.deathTime > 0 ? e.deathTime + pt : 0;
		s.shadowRadius = d.height() * 0.35f;
		MechaPose.Anim a = s.anim;
		a.walkPos = Mth.lerp(pt, e.animPosO, e.animPos);
		a.walkSpeed = Mth.lerp(pt, e.animSpeedO, e.animSpeed);
		a.air = Mth.lerp(pt, e.airO, e.air);
		a.land = Mth.lerp(pt, e.landO, e.land);
		a.action = Mth.lerp(pt, e.actionAnimO, e.actionAnim);
		a.age = e.tickCount + pt;
		a.shield = e.shield() > 0;
		a.attack = 0;
		a.headYaw = 0;
		a.headPitch = 0;
		a.hideHead = false;
		if (e.getControllingPassenger() instanceof Player p) {
			a.attack = p.getAttackAnim(pt);
			a.headYaw = Mth.clamp(Mth.wrapDegrees(Mth.rotLerp(pt, p.yHeadRotO, p.yHeadRot) - s.bodyRot), -65f, 65f);
			a.headPitch = Mth.clamp(Mth.lerp(pt, p.xRotO, p.getXRot()), -35f, 35f);
			Minecraft mc = Minecraft.getInstance();
			// Em primeira pessoa o piloto está dentro da cabeça: não desenha ela
			a.hideHead = p == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson();
		}
	}

	@Override
	public void submit(State s, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(180f - s.bodyRot));
		if (s.deathTime > 0) {
			float f = Math.min(1f, Math.max(0, (s.deathTime - 1f) / 20f * 1.6f));
			pose.mulPose(Axis.ZP.rotationDegrees(f * 90f));
		}
		MechaPose.submit(s.parts, pose, collector, s.lightCoords, s.anim, 6, -1, 0);
		pose.popPose();
		super.submit(s, pose, collector, camera);
	}
}
