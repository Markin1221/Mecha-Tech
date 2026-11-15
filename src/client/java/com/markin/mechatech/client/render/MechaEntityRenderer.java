package com.markin.mechatech.client.render;

import com.markin.mechatech.entity.MechaEntity;
import com.markin.mechatech.mecha.MechaData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;

/** Desenha o mecha montando os modelos 3D das peças. */
public class MechaEntityRenderer extends EntityRenderer<MechaEntity, MechaEntityRenderer.State> {
	private final ItemModelResolver items;

	public static class State extends EntityRenderState {
		final MechaPose.Parts parts = new MechaPose.Parts();
		float bodyRot, walkPos, walkSpeed, deathTime;
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
	public void extractRenderState(MechaEntity e, State s, float partial) {
		super.extractRenderState(e, s, partial);
		MechaData d = e.data();
		MechaPose.update(items, s.parts, d, e.level());
		s.bodyRot = Mth.rotLerp(partial, e.yBodyRotO, e.yBodyRot);
		s.walkPos = e.walkAnimation.position(partial);
		s.walkSpeed = e.walkAnimation.speed(partial);
		s.deathTime = e.deathTime > 0 ? e.deathTime + partial : 0;
		s.shadowRadius = d.height() * 0.35f;
	}

	@Override
	public void submit(State s, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(180f - s.bodyRot));
		if (s.deathTime > 0) {
			float f = Math.min(1f, (s.deathTime - 1f) / 20f * 1.6f);
			pose.mulPose(Axis.ZP.rotationDegrees(Math.max(0, f) * 90f));
		}
		MechaPose.submit(s.parts, pose, collector, s.lightCoords, s.walkPos, s.walkSpeed, 6, -1, 0);
		pose.popPose();
		super.submit(s, pose, collector, camera);
	}
}
