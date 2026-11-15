package com.markin.mechatech.client.screen;

import java.util.List;

import com.markin.mechatech.MechaTech;
import com.markin.mechatech.block.MechaAssemblerBlock;
import com.markin.mechatech.block.entity.MechaAssemblerBlockEntity;
import com.markin.mechatech.mecha.MechaData;
import com.markin.mechatech.menu.MechaAssemblerMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class MechaAssemblerScreen extends AbstractContainerScreen<MechaAssemblerMenu> {
	private static final Identifier TEXTURE = MechaTech.id("textures/gui/container/mecha_assembler.png");
	private static final int W = 176, H = 222;
	private static final int CYAN = 0xFF7DF9E8, GRAY = 0xFF8FA3B8, WHITE = 0xFFE8EEF5, RED = 0xFFFF6060, GREEN = 0xFF70F090, ORANGE = 0xFFFFB050;
	private static final int BAR_X = 160, BAR_Y = 20, BAR_W = 10, BAR_H = 52;
	private static final String[] SLOT_KEYS = {"head", "torso", "arm_left", "arm_right", "legs", "backpack"};

	public MechaAssemblerScreen(MechaAssemblerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, W, H);
		this.titleLabelX = 10;
		this.titleLabelY = 5;
		this.inventoryLabelY = 10000;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(g, mouseX, mouseY, partialTick);
		g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0f, 0f, W, H, 256, 256);
		int pw = Math.round(menu.progress() * 22);
		if (pw > 0) g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 97, topPos + 42, 180f, 60f, pw, 16, 256, 256);
		int eh = Math.round((float) menu.energy() / menu.capacity() * BAR_H);
		if (eh > 0) g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + BAR_X, topPos + BAR_Y + BAR_H - eh,
			180f, 0f + BAR_H - eh, BAR_W, eh, 256, 256);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		g.text(font, title, titleLabelX, titleLabelY, CYAN, true);
		MechaData d = menu.preview();
		int size = Math.max(3, menu.size());
		int x = 10, y = 91;
		if (MechaData.present(d.torso())) {
			boolean over = d.overweight();
			g.text(font, Component.translatable("stat.mechatech.load", d.weight(), d.capacity()), x, y, over ? RED : GRAY, false);
			g.text(font, Component.translatable("stat.mechatech.armor", d.armor()), 92, y, 0xFF7FA8FF, false);
			y += 10;
			g.text(font, Component.translatable("stat.mechatech.power", d.power()), x, y, 0xFFFF7F7F, false);
			g.text(font, Component.translatable("stat.mechatech.speed", d.speed()), 92, y, GREEN, false);
			y += 10;
			boolean big = d.scale() > MechaAssemblerBlock.maxScale(size);
			g.text(font, Component.translatable("stat.mechatech.height", String.format("%.1f", d.height()), d.scale()), x, y, big ? RED : WHITE, false);
			g.text(font, Component.translatable("stat.mechatech.max_scale", MechaAssemblerBlock.maxScale(size)), 92, y, GRAY, false);
		} else {
			g.text(font, Component.translatable("gui.mechatech.asm_hint"), x, y, GRAY, false);
		}
		int st = menu.status();
		int color = switch (st) {
			case MechaAssemblerBlockEntity.BUILDING, MechaAssemblerBlockEntity.CHARGING -> CYAN;
			case MechaAssemblerBlockEntity.READY -> GREEN;
			case MechaAssemblerBlockEntity.IDLE, MechaAssemblerBlockEntity.MISSING -> GRAY;
			case MechaAssemblerBlockEntity.OUTPUT_FULL, MechaAssemblerBlockEntity.NO_POWER -> ORANGE;
			default -> RED;
		};
		Component msg = st == MechaAssemblerBlockEntity.BUILDING
			? Component.translatable("gui.mechatech.asm_status.6", Math.round(menu.progress() * 100))
			: Component.translatable("gui.mechatech.asm_status." + st);
		g.text(font, Component.literal("▶ ").append(msg), x, 121, color, false);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(g, mouseX, mouseY, partialTick);
		int x = mouseX - leftPos, y = mouseY - topPos;
		if (x >= BAR_X && x < BAR_X + BAR_W && y >= BAR_Y && y < BAR_Y + BAR_H) {
			g.setComponentTooltipForNextFrame(font, List.of(Component.translatable("gui.mechatech.energy"),
				Component.literal(menu.energy() + " / " + menu.capacity() + " EM").withColor(0x7DF9E8)), mouseX, mouseY);
			return;
		}
		for (int i = 0; i < 6; i++) {
			int sx = MechaAssemblerMenu.POS[i][0], sy = MechaAssemblerMenu.POS[i][1];
			if (x >= sx && x < sx + 16 && y >= sy && y < sy + 16 && !menu.slots.get(i).hasItem()) {
				g.setComponentTooltipForNextFrame(font, List.of(Component.translatable("gui.mechatech.slot." + SLOT_KEYS[i])), mouseX, mouseY);
			}
		}
	}
}
