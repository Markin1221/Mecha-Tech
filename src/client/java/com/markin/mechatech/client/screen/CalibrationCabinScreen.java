package com.markin.mechatech.client.screen;

import java.util.List;

import com.markin.mechatech.MechaTech;
import com.markin.mechatech.component.ModComponents;
import com.markin.mechatech.exo.ExoArmorItem;
import com.markin.mechatech.exo.ExoModuleItem;
import com.markin.mechatech.menu.CalibrationCabinMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class CalibrationCabinScreen extends AbstractContainerScreen<CalibrationCabinMenu> {
	private static final Identifier TEXTURE = MechaTech.id("textures/gui/container/calibration_cabin.png");
	private static final int W = 216, H = 240;
	private static final int CYAN = 0xFF7DF9E8, GRAY = 0xFF8FA3B8, ORANGE = 0xFFFFB050, RED = 0xFFFF6060;

	// Layout (bate com a textura)
	private static final int ROW_Y = 18, ROW_H = 28;
	private static final int PIECE_BAR_X = 97, PIECE_BAR_W = 37;  // barra de energia de cada peça
	private static final int INFO_X = 149, INFO_Y = 67;
	private static final int CABIN_BAR_X = 148, CABIN_BAR_Y = 106, CABIN_BAR_W = 60;
	private static final int PAL_X = 148, PAL_Y = 118, SW = 6, GAP = 1;

	public CalibrationCabinScreen(CalibrationCabinMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, W, H);
		this.titleLabelX = 10;
		this.titleLabelY = 5;
	}

	@Override
	protected void init() {
		super.init();
		this.addRenderableWidget(Button.builder(Component.translatable("gui.mechatech.assemble"),
				b -> click(CalibrationCabinMenu.BUTTON_ASSEMBLE))
			.bounds(leftPos + 148, topPos + 44, 60, 16).build());
	}

	private void click(int id) {
		if (this.minecraft != null && this.minecraft.gameMode != null) {
			this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
		}
	}

	private ItemStack piece(int i) {
		if (this.minecraft == null || this.minecraft.player == null) return ItemStack.EMPTY;
		return this.minecraft.player.getItemBySlot(CalibrationCabinMenu.slotOf(i));
	}

	/** Gasto máximo dos módulos instalados (EM/s). */
	private int maxConsumption() {
		int total = 0;
		for (int i = 0; i < 4; i++) {
			ItemStack p = piece(i);
			if (!ExoArmorItem.isExo(p)) continue;
			for (ItemStack m : p.getOrDefault(ModComponents.EXO_MODULES, ItemContainerContents.EMPTY).nonEmptyItemCopyStream().toList()) {
				if (m.getItem() instanceof ExoModuleItem mod) total += mod.module.costPerSecond;
			}
		}
		return total;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(g, mouseX, mouseY, partialTick);
		g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0f, 0f, W, H, 256, 256);

		// Jogador em 3D (segue o mouse)
		if (this.minecraft != null && this.minecraft.player != null) {
			InventoryScreen.extractEntityInInventoryFollowsMouse(g, leftPos + 10, topPos + 18, leftPos + 70, topPos + 138,
				44, 0.0625F, mouseX, mouseY, this.minecraft.player);
		}

		// Energia de cada peça (atlas 64,242 — 37x3)
		for (int i = 0; i < 4; i++) {
			ItemStack p = piece(i);
			if (!ExoArmorItem.isExo(p)) continue;
			int w = Math.round((float) ExoArmorItem.getEnergy(p) / ExoArmorItem.MAX_ENERGY * PIECE_BAR_W);
			if (w > 0) {
				g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + PIECE_BAR_X, topPos + ROW_Y + i * ROW_H + 23,
					64f, 242f, w, 3, 256, 256);
			}
		}

		// Energia da cabine (atlas 0,242 — 60x5)
		int cw = Math.round((float) menu.getCabinEnergy() / menu.getCabinCapacity() * CABIN_BAR_W);
		if (cw > 0) {
			g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + CABIN_BAR_X, topPos + CABIN_BAR_Y, 0f, 242f, cw, 5, 256, 256);
		}

		// Paleta
		for (int i = 0; i < 16; i++) {
			int x = leftPos + PAL_X + (i % 8) * (SW + GAP);
			int y = topPos + PAL_Y + (i / 8) * (SW + GAP);
			g.fill(x, y, x + SW, y + SW, 0xFF000000 | DyeColor.byId(i).getTextureDiffuseColor());
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		g.text(this.font, this.title, titleLabelX, titleLabelY, CYAN, true);

		// Painel de informações
		int cabin = menu.getCabinEnergy();
		g.text(font, Component.translatable("gui.mechatech.cabin_energy"), INFO_X, INFO_Y, GRAY, false);
		g.text(font, Component.literal(shortNumber(cabin) + " EM"), INFO_X, INFO_Y + 9, cabin > 0 ? CYAN : RED, false);
		int use = maxConsumption();
		g.text(font, Component.translatable("gui.mechatech.consumption"), INFO_X, INFO_Y + 19, GRAY, false);
		g.text(font, Component.literal(use + " EM/s"), INFO_X, INFO_Y + 28, use > 0 ? ORANGE : CYAN, false);
	}

	private static String shortNumber(int n) {
		return n >= 10000 ? (n / 1000) + "k" : n >= 1000 ? String.format("%.1fk", n / 1000f) : String.valueOf(n);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(g, mouseX, mouseY, partialTick);
		int x = mouseX - leftPos, y = mouseY - topPos;

		for (int i = 0; i < 4; i++) {
			int by = ROW_Y + i * ROW_H + 22;
			if (x >= PIECE_BAR_X - 1 && x < PIECE_BAR_X + PIECE_BAR_W + 1 && y >= by && y < by + 5) {
				ItemStack p = piece(i);
				Component v = ExoArmorItem.isExo(p)
					? Component.literal(ExoArmorItem.getEnergy(p) + " / " + ExoArmorItem.MAX_ENERGY + " EM").withColor(0x7DF9E8)
					: Component.translatable("gui.mechatech.no_exo").withColor(0xFF7070);
				g.setComponentTooltipForNextFrame(font, List.of(Component.translatable("gui.mechatech.piece_energy"), v), mouseX, mouseY);
				return;
			}
		}
		if (x >= CABIN_BAR_X && x < CABIN_BAR_X + CABIN_BAR_W && y >= CABIN_BAR_Y - 1 && y < CABIN_BAR_Y + 6) {
			g.setComponentTooltipForNextFrame(font, List.of(Component.translatable("gui.mechatech.cabin_energy"),
				Component.literal(menu.getCabinEnergy() + " / " + menu.getCabinCapacity() + " EM").withColor(0x7DF9E8)), mouseX, mouseY);
			return;
		}
		int sw = swatchAt(x, y);
		if (sw >= 0) {
			g.setComponentTooltipForNextFrame(font, List.of(
				Component.translatable("color.minecraft." + DyeColor.byId(sw).getName()),
				Component.translatable("gui.mechatech.paint_hint").withColor(0x808890)), mouseX, mouseY);
		}
	}

	private int swatchAt(int x, int y) {
		int cx = x - PAL_X, cy = y - PAL_Y;
		if (cx < 0 || cy < 0) return -1;
		int col = cx / (SW + GAP), row = cy / (SW + GAP);
		if (col >= 8 || row >= 2 || cx % (SW + GAP) >= SW || cy % (SW + GAP) >= SW) return -1;
		return row * 8 + col;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int sw = swatchAt((int) event.x() - leftPos, (int) event.y() - topPos);
		if (sw >= 0) {
			boolean clear = event.button() == 1;
			click(clear ? CalibrationCabinMenu.BUTTON_PAINT_CLEAR : CalibrationCabinMenu.BUTTON_PAINT_FIRST + sw);
			// Mostra a cor na hora (o servidor confirma logo depois)
			if (this.minecraft != null && this.minecraft.player != null) {
				CalibrationCabinMenu.applyPaint(this.minecraft.player, clear ? null : DyeColor.byId(sw).getTextureDiffuseColor() & 0xFFFFFF);
			}
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}
}
