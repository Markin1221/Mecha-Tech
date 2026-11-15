package com.markin.mechatech.client.render;

import com.markin.mechatech.block.MechaAssemblerBlock;
import com.markin.mechatech.block.entity.MechaAssemblerBlockEntity;
import com.markin.mechatech.items.MechaItem;
import com.markin.mechatech.mecha.MechaData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Mostra o mecha sendo montado peça por peça dentro da Montadora (e o mecha pronto parado lá). */
public class MechaAssemblerRenderer implements BlockEntityRenderer<MechaAssemblerBlockEntity, MechaAssemblerRenderer.State> {
	private final ItemModelResolver items;

	public MechaAssemblerRenderer(BlockEntityRendererProvider.Context context) {
		this.items = context.itemModelResolver();
	}

	public static class State extends BlockEntityRenderState {
		final MechaPose.Parts parts = new MechaPose.Parts();
		boolean visible;
		int shown, size;
		float installing, time;
		Direction facing = Direction.NORTH;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(MechaAssemblerBlockEntity be, State s, float partial, Vec3 camera,
								   ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, partial, camera, crumbling);
		s.facing = be.getBlockState().getValue(MechaAssemblerBlock.FACING);
		s.size = be.size();
		s.time = be.getLevel() == null ? 0 : be.getLevel().getGameTime() + partial;
		s.visible = false;
		MechaData d;
		if (be.status() == MechaAssemblerBlockEntity.BUILDING || be.status() == MechaAssemblerBlockEntity.NO_POWER) {
			d = be.assembled();
			float elapsed = be.status() == MechaAssemblerBlockEntity.BUILDING
				? Math.min(be.total(), s.time - be.buildStart()) : be.progress();
			int k = (int) (elapsed / MechaAssemblerBlockEntity.PART_TIME);
			s.shown = skipMissing(d, k);
			s.installing = (elapsed % MechaAssemblerBlockEntity.PART_TIME) / MechaAssemblerBlockEntity.PART_TIME;
			s.visible = true;
		} else if (be.output().getItem() instanceof MechaItem) {
			d = MechaItem.data(be.output());
			s.shown = 6;
			s.installing = -1;
			s.visible = d.complete();
		} else {
			d = MechaData.EMPTY;
		}
		if (s.visible) MechaPose.update(items, s.parts, d, be.getLevel());
	}

	/** A montagem conta só peças presentes (mochila é opcional e é a última). */
	private static int skipMissing(MechaData d, int k) {
		return k;
	}

	@Override
	public void submit(State s, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!s.visible) return;
		Direction r = MechaAssemblerBlock.right(s.facing), b = MechaAssemblerBlock.back(s.facing);
		double h = s.size / 2.0 - 0.5;
		pose.pushPose();
		pose.translate(0.5 + h * (r.getStepX() + b.getStepX()), 0.25, 0.5 + h * (r.getStepZ() + b.getStepZ()));
		pose.mulPose(Axis.YP.rotationDegrees(180f - s.facing.toYRot()));
		MechaPose.Anim a = new MechaPose.Anim();
		a.age = s.time;
		MechaPose.submit(s.parts, pose, collector, 0xF000F0, a, s.shown, s.installing, s.size * 0.55f);
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 96;
	}
}
