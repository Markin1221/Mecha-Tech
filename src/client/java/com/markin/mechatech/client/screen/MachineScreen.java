package com.markin.mechatech.client.screen;

import java.util.List;

import com.markin.mechatech.menu.MachineMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Base das telas de máquina: fundo temático + barra de energia com tooltip. */
public abstract class MachineScreen<M extends MachineMenu> extends AbstractContainerScreen<M> {
	protected static final int TITLE_COLOR = 0xFF7DF9E8;
	// Barra de energia: posição na tela e no atlas (preenchimento cheio)
	protected static final int BAR_X = 152, BAR_Y = 17, BAR_W = 12, BAR_H = 52;
	protected static final int BAR_U = 176, BAR_V = 0;

	protected final Identifier texture;

	protected MachineScreen(M menu, Inventory inventory, Component title, Identifier texture) {
		super(menu, inventory, title, 176, 166);
		this.texture = texture;
		this.titleLabelX = 10;
		this.titleLabelY = 5;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(g, mouseX, mouseY, partialTick);
		g.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0f, 0f, imageWidth, imageHeight, 256, 256);

		// Energia: enche de baixo pra cima
		int h = Math.round(menu.getEnergyRatio() * BAR_H);
		if (h > 0) {
			g.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + BAR_X, topPos + BAR_Y + BAR_H - h,
				BAR_U, BAR_V + BAR_H - h, BAR_W, h, 256, 256);
		}
		extractMachine(g);
	}

	/** Desenha as partes animadas de cada máquina. */
	protected abstract void extractMachine(GuiGraphicsExtractor g);

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		g.text(this.font, this.title, this.titleLabelX, this.titleLabelY, TITLE_COLOR, true);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(g, mouseX, mouseY, partialTick);
		int x = mouseX - leftPos, y = mouseY - topPos;
		if (x >= BAR_X && x < BAR_X + BAR_W && y >= BAR_Y && y < BAR_Y + BAR_H) {
			g.setComponentTooltipForNextFrame(this.font, List.of(
				Component.translatable("gui.mechatech.energy"),
				Component.literal(menu.getEnergy() + " / " + menu.getCapacity() + " EM").withColor(0x7DF9E8)
			), mouseX, mouseY);
		}
	}
}
