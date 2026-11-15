package com.markin.mechatech.client.render;

import java.util.EnumMap;
import java.util.Map;

import com.markin.mechatech.MechaTech;
import com.markin.mechatech.exo.ExoArmorItem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/** Desenha o exoesqueleto 3D: estrutura (pintável), detalhes escuros e luzes que brilham no escuro. */
public class ExoArmorRenderer implements ArmorRenderer {
	public static final int DEFAULT_COLOR = 0x4A78D8;
	private static final Identifier BASE = MechaTech.id("textures/entity/exo/exo_base.png");
	private static final Identifier DETAIL = MechaTech.id("textures/entity/exo/exo_detail.png");
	private static final Identifier GLOW = MechaTech.id("textures/entity/exo/exo_glow.png");
	private static final int FULL_BRIGHT = 0xF000F0;

	private final Map<EquipmentSlot, ExoArmorModel> models = new EnumMap<>(EquipmentSlot.class);

	public ExoArmorRenderer(EntityRendererProvider.Context context) {
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			models.put(slot, new ExoArmorModel(context.bakeLayer(ExoArmorModel.LAYER), slot));
		}
	}

	@Override
	public void render(PoseStack pose, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState state,
					   EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
		ExoArmorModel model = models.get(slot);
		if (model == null) return;
		int color = 0xFF000000 | (DyedItemColor.getOrDefault(stack, DEFAULT_COLOR) & 0xFFFFFF);

		ArmorRenderer.submitTransformCopyingModel(contextModel, state, model, state, false, collector, pose,
			RenderTypes.armorCutoutNoCull(BASE), light, OverlayTexture.NO_OVERLAY, color, null);
		ArmorRenderer.submitTransformCopyingModel(contextModel, state, model, state, false, collector, pose,
			RenderTypes.armorCutoutNoCull(DETAIL), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null);
		// Luzes só acendem com energia
		if (ExoArmorItem.getEnergy(stack) > 0) {
			ArmorRenderer.submitTransformCopyingModel(contextModel, state, model, state, false, collector, pose,
				RenderTypes.eyes(GLOW), FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null);
		}
	}
}
