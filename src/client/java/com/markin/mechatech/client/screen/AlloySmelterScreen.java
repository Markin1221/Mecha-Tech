package com.markin.mechatech.client.screen;

import com.markin.mechatech.MechaTech;
import com.markin.mechatech.menu.AlloySmelterMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AlloySmelterScreen extends MachineScreen<AlloySmelterMenu> {

	public AlloySmelterScreen(AlloySmelterMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, MechaTech.id("textures/gui/container/alloy_smelter.png"));
	}

	@Override
	protected void extractMachine(GuiGraphicsExtractor g) {
		// Seta de progresso (atlas 176,76 — 24x17), enche da esquerda pra direita
		int w = Math.round(menu.getProgress() * 24);
		if (w > 0) {
			g.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 79, topPos + 36, 176, 76, w, 17, 256, 256);
			// Brasa acesa embaixo das entradas (atlas 176,56 — 14x14)
			g.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 85, topPos + 58, 176, 56, 14, 14, 256, 256);
		}
	}
}
