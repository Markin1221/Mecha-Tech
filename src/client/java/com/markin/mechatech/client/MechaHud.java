package com.markin.mechatech.client;

import com.markin.mechatech.block.entity.PartForgeBlockEntity;
import com.markin.mechatech.entity.MechaAbilities;
import com.markin.mechatech.entity.MechaEntity;
import com.markin.mechatech.mecha.MechaData;
import com.markin.mechatech.mecha.MechaPartData;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/** Painel do piloto: integridade, energia, velocidade e habilidades das peças. */
public final class MechaHud {
	private static final int CYAN = 0xFF7DF9E8, CYAN_D = 0xFF1FB3A6, DARK = 0xB00A1418, EDGE = 0xFF2A4A50,
		WHITE = 0xFFE8EEF5, GRAY = 0xFF8FA3B8, RED = 0xFFFF5A5A, ORANGE = 0xFFFFB050, GREEN = 0xFF70F090;
	private static double lastX, lastZ;
	private static float speed;

	private MechaHud() { }

	public static MechaEntity mecha() {
		Minecraft mc = Minecraft.getInstance();
		return mc.player != null && mc.player.getVehicle() instanceof MechaEntity m ? m : null;
	}

	public static void tick() {
		MechaEntity m = mecha();
		if (m == null) return;
		double dx = m.getX() - lastX, dz = m.getZ() - lastZ;
		lastX = m.getX(); lastZ = m.getZ();
		float v = (float) Math.sqrt(dx * dx + dz * dz) * 20f;
		speed += (Math.min(v, 60f) - speed) * 0.3f;
	}

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		MechaEntity m = mecha();
		Minecraft mc = Minecraft.getInstance();
		if (m == null || mc.options.hideGui) return;
		Font font = mc.font;
		MechaData d = m.data();
		int W = g.guiWidth(), H = g.guiHeight();
		float age = m.tickCount + delta.getGameTimeDeltaPartialTick(true);

		visor(g, W, H);

		int pw = Math.min(150, (W - 182) / 2 - 10);
		boolean top = pw < 110;
		if (top) pw = 140;
		int ph = 56;
		int lx = 6, ly = top ? 6 : H - ph - 6;
		int rx = W - pw - 6, ry = ly;

		// ===== Painel esquerdo: status =====
		panel(g, lx, ly, pw, ph);
		g.text(font, Component.translatable("hud.mechatech.title", d.scale()), lx + 6, ly + 4, CYAN, true);
		float hp = m.getHealth() / Math.max(1, m.getMaxHealth());
		float en = (float) m.energy() / Math.max(1, d.maxEnergy());
		bar(g, font, lx + 6, ly + 16, pw - 12, Component.translatable("hud.mechatech.integrity"),
			Math.round(m.getHealth()) + "/" + Math.round(m.getMaxHealth()), hp, hp > 0.3f ? 0xFFE0524A : 0xFFFF2020, 0xFF7A1E1A);
		boolean lowE = en < 0.15f;
		int ec = lowE && ((int) (age / 5) % 2 == 0) ? ORANGE : CYAN;
		bar(g, font, lx + 6, ly + 32, pw - 12, Component.translatable("hud.mechatech.energy"),
			Math.round(en * 100) + "%", en, ec, 0xFF0E5C58);
		g.text(font, Component.translatable("hud.mechatech.speed", String.format("%.1f", speed), m.getBlockY()), lx + 6, ly + 46, GRAY, false);

		// ===== Painel direito: habilidades =====
		panel(g, rx, ry, pw, ph);
		g.text(font, Component.translatable("hud.mechatech.systems"), rx + 6, ry + 4, CYAN, true);
		String act = MechaAbilities.activeName(d);
		int y = ry + 15;
		if (!act.isEmpty()) {
			icon(g, act, d, rx + 6, y);
			float cd = (float) m.cooldown() / m.cooldownMax();
			g.text(font, Component.translatable("part.mechatech." + act), rx + 26, y, WHITE, false);
			int bw = pw - 32;
			g.fill(rx + 26, y + 10, rx + 26 + bw, y + 13, 0xFF1A2A30);
			g.fill(rx + 26, y + 10, rx + 26 + Math.round(bw * (1 - cd)), y + 13, cd > 0 ? ORANGE : GREEN);
			if (m.shield() > 0) g.text(font, Component.translatable("hud.mechatech.shield_on"), rx + pw - 40, y, GREEN, false);
			y += 18;
			g.text(font, Component.translatable("hud.mechatech.use_key"), rx + 6, y, GRAY, false);
			y += 10;
		}
		StringBuilder passives = new StringBuilder();
		if (d.has("drill_arm")) passives.append("BROCA 3x3  ");
		if (d.has("saw_arm")) passives.append("SERRA  ");
		if (d.has("sensor_head")) passives.append("VISÃO  ");
		if (d.has("thermal_head")) passives.append("TÉRMICA  ");
		if (d.has("radar_head")) passives.append("RADAR  ");
		if (d.has("jet_backpack")) passives.append("JATO  ");
		if (passives.length() > 0 && y < ry + ph - 8) g.text(font, passives.toString().trim(), rx + 6, y, CYAN_D, false);

		// ===== Alertas =====
		if (m.energy() <= 0) {
			center(g, font, Component.translatable("hud.mechatech.no_energy"), W / 2, H / 2 + 20, ((int) (age / 6) % 2 == 0) ? RED : 0x00000000);
		} else if (lowE) {
			center(g, font, Component.translatable("hud.mechatech.low_energy"), W / 2, H / 2 + 20, ORANGE);
		}
		if (d.overweight()) center(g, font, Component.translatable("hud.mechatech.overweight"), W / 2, H / 2 + 32, ORANGE);
	}

	private static void icon(GuiGraphicsExtractor g, String variant, MechaData d, int x, int y) {
		for (MechaPartData p : d.parts()) {
			if (p.variant().equals(variant)) {
				ItemStack s = PartForgeBlockEntity.createPart(p.def(), p.scale());
				g.fakeItem(s, x, y - 2);
				return;
			}
		}
	}

	private static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
		g.fill(x, y, x + w, y + h, DARK);
		g.fill(x, y, x + w, y + 1, EDGE);
		g.fill(x, y + h - 1, x + w, y + h, EDGE);
		g.fill(x, y, x + 1, y + h, EDGE);
		g.fill(x + w - 1, y, x + w, y + h, EDGE);
		// cantos acesos
		g.fill(x, y, x + 8, y + 1, CYAN); g.fill(x, y, x + 1, y + 6, CYAN);
		g.fill(x + w - 8, y + h - 1, x + w, y + h, CYAN); g.fill(x + w - 1, y + h - 6, x + w, y + h, CYAN);
		g.fill(x + 4, y + 13, x + w - 4, y + 14, 0x602A4A50);
	}

	private static void bar(GuiGraphicsExtractor g, Font font, int x, int y, int w, Component label, String value,
							float f, int color, int back) {
		g.text(font, label, x, y, GRAY, false);
		g.text(font, value, x + w - font.width(value), y, WHITE, false);
		int by = y + 9;
		g.fill(x, by, x + w, by + 5, 0xFF0A0E12);
		g.fill(x, by, x + w, by + 5, (back & 0x00FFFFFF) | 0x60000000);
		int fw = Math.round(w * Mth.clamp(f, 0, 1));
		g.fillGradient(x, by, x + fw, by + 5, color, (color & 0x00FFFFFF) | 0xA0000000);
		for (int i = x + 6; i < x + w; i += 6) g.fill(i, by, i + 1, by + 5, 0x50000000);
		g.fill(x, by, x + fw, by + 1, 0x60FFFFFF);
	}

	private static void center(GuiGraphicsExtractor g, Font font, Component c, int x, int y, int color) {
		if ((color & 0xFF000000) == 0) return;
		g.text(font, c, x - font.width(c) / 2, y, color, true);
	}

	/** Moldura do visor nos cantos da tela. */
	private static void visor(GuiGraphicsExtractor g, int W, int H) {
		int c = 0x707DF9E8, L = 28, m = 3;
		g.fill(m, m, m + L, m + 1, c); g.fill(m, m, m + 1, m + L, c);
		g.fill(W - m - L, m, W - m, m + 1, c); g.fill(W - m - 1, m, W - m, m + L, c);
		g.fill(m, H - m - 1, m + L, H - m, c); g.fill(m, H - m - L, m + 1, H - m, c);
		g.fill(W - m - L, H - m - 1, W - m, H - m, c); g.fill(W - m - 1, H - m - L, W - m, H - m, c);
		// mira do mecha
		int cx = W / 2, cy = H / 2, r = 10, cc = 0x907DF9E8;
		g.fill(cx - r, cy - r, cx - r + 4, cy - r + 1, cc); g.fill(cx - r, cy - r, cx - r + 1, cy - r + 4, cc);
		g.fill(cx + r - 4, cy - r, cx + r, cy - r + 1, cc); g.fill(cx + r - 1, cy - r, cx + r, cy - r + 4, cc);
		g.fill(cx - r, cy + r - 1, cx - r + 4, cy + r, cc); g.fill(cx - r, cy + r - 4, cx - r + 1, cy + r, cc);
		g.fill(cx + r - 4, cy + r - 1, cx + r, cy + r, cc); g.fill(cx + r - 1, cy + r - 4, cx + r, cy + r, cc);
	}
}
