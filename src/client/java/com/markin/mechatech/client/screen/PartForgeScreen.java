package com.markin.mechatech.client.screen;

import java.util.List;

import com.markin.mechatech.MechaTech;
import com.markin.mechatech.mecha.MechaPartData;
import com.markin.mechatech.mecha.PartVariant;
import com.markin.mechatech.menu.PartForgeMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PartForgeScreen extends AbstractContainerScreen<PartForgeMenu> {
	private static final Identifier TEXTURE = MechaTech.id("textures/gui/container/part_forge.png");
	private static final int W = 176, H = 234;
	private static final int CYAN = 0xFF7DF9E8, GRAY = 0xFF8FA3B8, WHITE = 0xFFE8EEF5, RED = 0xFFFF6060, GREEN = 0xFF70F090;
	private static final int ROW1 = 19, ROW2 = 33, ROW3 = 47;
	private static final int REQ_Y = 66, STAT_X = 104;
	private static final int BAR_X = 156, BAR_Y = 103, BAR_W = 10, BAR_H = 36;

	public PartForgeScreen(PartForgeMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, W, H);
		this.titleLabelX = 10;
		this.titleLabelY = 5;
	}

	@Override
	protected void init() {
		super.init();
		int[][] arrows = {{0, ROW1}, {2, ROW2}, {4, ROW3}};
		for (int[] a : arrows) {
			int id = a[0], y = a[1];
			this.addRenderableWidget(Button.builder(Component.literal("◀"), b -> click(id))
				.bounds(leftPos + 52, topPos + y - 2, 14, 12).build());
			this.addRenderableWidget(Button.builder(Component.literal("▶"), b -> click(id + 1))
				.bounds(leftPos + 156, topPos + y - 2, 14, 12).build());
		}
	}

	private void click(int id) {
		if (this.minecraft != null && this.minecraft.gameMode != null) {
			this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(g, mouseX, mouseY, partialTick);
		g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0f, 0f, W, H, 256, 256);

		// Progresso (atlas 180,40 — 22x15)
		int pw = Math.round(menu.progress() * 22);
		if (pw > 0) g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 76, topPos + 113, 180f, 40f, pw, 15, 256, 256);
		// Energia (atlas 180,0 — 10x36)
		int eh = Math.round((float) menu.energy() / menu.capacity() * BAR_H);
		if (eh > 0) g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + BAR_X, topPos + BAR_Y + BAR_H - eh,
			180f, 0f + BAR_H - eh, BAR_W, eh, 256, 256);

		// Materiais necessários (ícones)
		PartVariant v = menu.variant();
		if (v != null) {
			List<PartVariant.Cost> costs = v.cost();
			for (int i = 0; i < costs.size() && i < 4; i++) {
				PartVariant.Cost c = costs.get(i);
				ItemStack icon = c.ingredient().items().findFirst().map(Holder::value).map(Item::getDefaultInstance).orElse(ItemStack.EMPTY);
				int x = leftPos + 10 + i * 22, y = topPos + REQ_Y;
				g.fakeItem(icon, x, y);
				boolean ok = menu.countOf(c) >= c.count();
				g.itemDecorations(this.font, icon, x, y, (ok ? "§a" : "§c") + c.count());
			}
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		g.text(font, title, titleLabelX, titleLabelY, CYAN, true);
		g.text(font, Component.translatable("gui.mechatech.part_type"), 10, ROW1, GRAY, false);
		g.text(font, Component.translatable("gui.mechatech.part_variant"), 10, ROW2, GRAY, false);
		g.text(font, Component.translatable("gui.mechatech.part_scale"), 10, ROW3, GRAY, false);

		g.text(font, Component.translatable("parttype.mechatech." + menu.type().id), 70, ROW1, WHITE, false);
		PartVariant v = menu.variant();
		if (v != null) g.text(font, Component.translatable("part.mechatech." + v.id()), 70, ROW2, WHITE, false);
		g.text(font, Component.literal(menu.scale() + "%"), 70, ROW3, WHITE, false);

		if (!menu.formed()) {
			g.text(font, Component.translatable("gui.mechatech.forge_incomplete_1"), STAT_X, 64, RED, false);
			g.text(font, Component.translatable("gui.mechatech.forge_incomplete_2"), STAT_X, 74, RED, false);
			return;
		}

		// Atributos da peça selecionada (com a escala escolhida)
		if (v != null) {
			MechaPartData d = new MechaPartData(v.id(), menu.scale());
			int y = REQ_Y - 4;
			y = stat(g, "weight", d.weight(), y, GRAY);
			if (d.armor() > 0) y = stat(g, "armor", d.armor(), y, 0xFF7FA8FF);
			if (d.power() > 0) y = stat(g, "power", d.power(), y, 0xFFFF7F7F);
			if (d.speed() > 0) y = stat(g, "speed", d.speed(), y, GREEN);
			if (d.capacity() > 0) y = stat(g, "capacity", d.capacity(), y, 0xFFFFC860);
			if (d.energy() > 0) stat(g, "energy", d.energy(), y, CYAN);
		}
	}

	private int stat(GuiGraphicsExtractor g, String key, int value, int y, int color) {
		if (y > REQ_Y + 26) return y;
		g.text(font, Component.translatable("stat.mechatech." + key, value), STAT_X, y, color, false);
		return y + 8;
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
		PartVariant v = menu.variant();
		if (v != null) {
			for (int i = 0; i < v.cost().size() && i < 4; i++) {
				int ix = 10 + i * 22;
				if (x >= ix && x < ix + 16 && y >= REQ_Y && y < REQ_Y + 16) {
					PartVariant.Cost c = v.cost().get(i);
					ItemStack icon = c.ingredient().items().findFirst().map(Holder::value).map(Item::getDefaultInstance).orElse(ItemStack.EMPTY);
					g.setComponentTooltipForNextFrame(font, List.of(icon.getHoverName(),
						Component.translatable("gui.mechatech.have_need", menu.countOf(c), c.count()).withColor(0xA0B0C0)), mouseX, mouseY);
				}
			}
		}
	}
}
