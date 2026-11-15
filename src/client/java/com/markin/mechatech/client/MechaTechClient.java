package com.markin.mechatech.client;

import com.markin.mechatech.client.screen.AlloySmelterScreen;
import com.markin.mechatech.client.screen.CalibrationCabinScreen;
import com.markin.mechatech.client.screen.CombustionGeneratorScreen;
import com.markin.mechatech.client.screen.MechanicalWorkbenchScreen;
import com.markin.mechatech.client.screen.PartForgeScreen;
import com.markin.mechatech.menu.ModMenus;

import com.markin.mechatech.client.render.ExoArmorModel;
import com.markin.mechatech.client.render.PartForgeRenderer;
import com.markin.mechatech.block.entity.ModBlockEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import com.markin.mechatech.client.render.ExoArmorRenderer;
import com.markin.mechatech.items.ModItems;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

public class MechaTechClient implements ClientModInitializer {
	private static boolean wasRiding;
	private static net.minecraft.client.CameraType previousCamera;

	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenus.MECHANICAL_WORKBENCH, MechanicalWorkbenchScreen::new);
		MenuScreens.register(ModMenus.COMBUSTION_GENERATOR, CombustionGeneratorScreen::new);
		MenuScreens.register(ModMenus.ALLOY_SMELTER, AlloySmelterScreen::new);
		MenuScreens.register(ModMenus.CALIBRATION_CABIN, CalibrationCabinScreen::new);
		MenuScreens.register(ModMenus.PART_FORGE, PartForgeScreen::new);

		// Montadora + mecha
		BlockEntityRenderers.register(ModBlockEntities.MECHA_ASSEMBLER, com.markin.mechatech.client.render.MechaAssemblerRenderer::new);
		net.minecraft.client.renderer.entity.EntityRenderers.register(com.markin.mechatech.entity.ModEntities.MECHA,
			com.markin.mechatech.client.render.MechaEntityRenderer::new);
		MenuScreens.register(com.markin.mechatech.menu.ModMenus.MECHA_ASSEMBLER, com.markin.mechatech.client.screen.MechaAssemblerScreen::new);
		// Câmera em 3ª pessoa enquanto pilota o mecha
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			boolean riding = mc.player != null && mc.player.getVehicle() instanceof com.markin.mechatech.entity.MechaEntity;
			if (riding && !wasRiding) {
				previousCamera = mc.options.getCameraType();
				mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
			} else if (!riding && wasRiding && previousCamera != null) {
				mc.options.setCameraType(previousCamera);
			}
			wasRiding = riding;
		});

		// Animação da Forja de Peças
		BlockEntityRenderers.register(ModBlockEntities.PART_FORGE, PartForgeRenderer::new);

		// Exoesqueleto 3D
		ModelLayerRegistry.registerModelLayer(ExoArmorModel.LAYER, ExoArmorModel::createLayer);
		ArmorRenderer.register(ExoArmorRenderer::new,
			ModItems.EXO_HELMET, ModItems.EXO_CHESTPLATE, ModItems.EXO_LEGGINGS, ModItems.EXO_BOOTS);
	}
}
