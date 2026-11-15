package com.markin.mechatech.client.screen;

import com.markin.mechatech.MechaTech;
import com.markin.mechatech.menu.MechanicalWorkbenchMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class MechanicalWorkbenchScreen extends AbstractContainerScreen<MechanicalWorkbenchMenu> {
	private static final Identifier TEXTURE = MechaTech.id("textures/gui/container/mechanical_workbench.png");
	private static final int WIDTH = 176;
	private static final int HEIGHT = 184;
	private static final int TITLE_COLOR = 0xFF7DF9E8;

	public MechanicalWorkbenchScreen(MechanicalWorkbenchMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, WIDTH, HEIGHT);
		this.titleLabelX = 21;
		this.titleLabelY = 4;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, WIDTH, HEIGHT, 256, 256);

		// Quando tem receita pronta: seta de energia acesa + LED verde
		if (this.menu.getSlot(0).hasItem()) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 103, this.topPos + 43, 176.0F, 0.0F, 22, 17, 256, 256);
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 132, this.topPos + 72, 176.0F, 20.0F, 4, 3, 256, 256);
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		// Só o título, em ciano (o rótulo "Inventário" foi tirado pro visual ficar limpo)
		graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, TITLE_COLOR, true);
	}
}
