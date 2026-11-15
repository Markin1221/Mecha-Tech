package com.markin.mechatech.client.render;

import com.markin.mechatech.block.PartForgeBlock;
import com.markin.mechatech.block.entity.PartForgeBlockEntity;
import com.markin.mechatech.items.ModItems;
import com.markin.mechatech.mecha.MechaParts;
import com.markin.mechatech.mecha.PartVariant;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Animação da Forja de Peças: a peça vai sendo montada no centro (cresce, gira e sobe),
 * enquanto dois braços robóticos trabalham nela.
 */
public class PartForgeRenderer implements BlockEntityRenderer<PartForgeBlockEntity, PartForgeRenderer.State> {
	private final ItemModelResolver items;

	public PartForgeRenderer(BlockEntityRendererProvider.Context context) {
		this.items = context.itemModelResolver();
	}

	public static class State extends BlockEntityRenderState {
		final ItemStackRenderState part = new ItemStackRenderState();
		final ItemStackRenderState armLeft = new ItemStackRenderState();
		final ItemStackRenderState armRight = new ItemStackRenderState();
		boolean building;
		float progress, time;
		Direction facing = Direction.NORTH;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(PartForgeBlockEntity be, State s, float partialTick, Vec3 camera,
								   ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, partialTick, camera, crumbling);
		s.facing = be.getBlockState().getValue(PartForgeBlock.FACING);
		long now = be.getLevel() == null ? 0 : be.getLevel().getGameTime();
		s.time = now + partialTick;
		PartVariant v = MechaParts.byId(be.buildingVariant());
		s.building = v != null;
		if (s.building) {
			s.progress = Mth.clamp((s.time - be.buildStart()) / be.buildTotal(), 0f, 1f);
			ItemStack stack = PartForgeBlockEntity.createPart(v, be.buildScale());
			items.updateForTopItem(s.part, stack, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
		} else {
			s.part.clear();
		}
		ItemStack arm = new ItemStack(ModItems.MINER_ARM);
		// NONE = usa o modelo 3D do braço (o mesmo da mão), sem ajuste de exibição
		items.updateForTopItem(s.armLeft, arm, ItemDisplayContext.NONE, be.getLevel(), null, 1);
		items.updateForTopItem(s.armRight, arm, ItemDisplayContext.NONE, be.getLevel(), null, 2);
	}

	@Override
	public void submit(State s, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		Direction r = PartForgeBlock.right(s.facing), b = PartForgeBlock.back(s.facing);
		int light = 0xF000F0;

		pose.pushPose();
		// Centro da máquina (a entidade fica no canto da frente-esquerda-baixo)
		pose.translate(0.5 + 0.5 * (r.getStepX() + b.getStepX()), 0.0, 0.5 + 0.5 * (r.getStepZ() + b.getStepZ()));
		pose.mulPose(Axis.YP.rotationDegrees(180f - s.facing.toYRot()));

		// ===== Peça sendo montada =====
		if (s.building && !s.part.isEmpty()) {
			float p = s.progress;
			float grow = 0.25f + 0.75f * easeOut(p);
			pose.pushPose();
			pose.translate(0, 0.95 + 0.05 * Mth.sin(s.time * 0.08f) + 0.1 * p, 0);
			pose.mulPose(Axis.YP.rotationDegrees(s.time * (6f - 4f * p)));
			pose.scale(1.3f * grow, 1.3f * grow, 1.3f * grow);
			s.part.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}

		// ===== Braços robóticos (um de cada lado) =====
		float work = s.building ? Mth.sin(s.time * 0.35f) : 0f;
		for (int side = -1; side <= 1; side += 2) {
			pose.pushPose();
			pose.translate(side * (0.56 - 0.06 * Math.abs(work)), 1.0 + 0.05 * work * side, 0.0);
			pose.mulPose(Axis.ZP.rotationDegrees(side * (95f + 15f * work)));
			pose.scale(0.6f, 0.6f, 0.6f);
			(side < 0 ? s.armLeft : s.armRight).submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}
		pose.popPose();
	}

	private static float easeOut(float t) {
		return 1f - (1f - t) * (1f - t);
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}
}
