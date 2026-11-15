package com.markin.mechatech.client.screen;

import com.markin.mechatech.MechaTech;
import com.markin.mechatech.menu.CombustionGeneratorMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CombustionGeneratorScreen extends MachineScreen<CombustionGeneratorMenu> {

	public CombustionGeneratorScreen(CombustionGeneratorMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, MechaTech.id("textures/gui/container/combustion_generator.png"));
	}

	@Override
	protected void extractMachine(GuiGraphicsExtractor g) {
		// Chama: diminui conforme o combustível acaba (atlas em 176,56 — 14x14)
		if (menu.isBurning()) {
			int h = Math.max(1, Math.round(menu.getBurnRatio() * 14));
			g.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 81, topPos + 36 + 14 - h, 176, 56 + 14 - h, 14, h, 256, 256);
			// LED aceso (atlas 176,72 — 4x3)
			g.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 118, topPos + 58, 176, 72, 4, 3, 256, 256);
		}
	}
}
