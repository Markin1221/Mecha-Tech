package com.markin.mechatech.exo;

import java.util.function.Consumer;

import com.markin.mechatech.component.ModComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Peça da exo-armadura. Não tem durabilidade: tem ENERGIA.
 * Sem energia ela não quebra, só perde os poderes e a proteção.
 */
public class ExoArmorItem extends Item {
	public static final int MAX_ENERGY = 25_000; // por peça
	public final ArmorType type;

	public ExoArmorItem(ArmorType type, Properties properties) {
		super(properties);
		this.type = type;
	}

	public static boolean isExo(ItemStack stack) {
		return stack.getItem() instanceof ExoArmorItem;
	}

	public static int getEnergy(ItemStack stack) {
		return stack.getOrDefault(ModComponents.EXO_ENERGY, 0);
	}

	public static void setEnergy(ItemStack stack, int value) {
		int v = Mth.clamp(value, 0, MAX_ENERGY);
		if (v != getEnergy(stack)) stack.set(ModComponents.EXO_ENERGY, v);
	}

	public static boolean hasPower(ItemStack stack) {
		return isExo(stack) && getEnergy(stack) > 0;
	}

	// ===== Barra de energia no lugar da durabilidade =====
	@Override
	public boolean isBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13f * getEnergy(stack) / MAX_ENERGY);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		float f = (float) getEnergy(stack) / MAX_ENERGY;
		return f > 0.25f ? 0x4FE8D6 : f > 0 ? 0xFFA030 : 0xFF4040;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
								Consumer<Component> tooltip, TooltipFlag flag) {
		int e = getEnergy(stack);
		tooltip.accept(Component.translatable("tooltip.mechatech.exo_energy", e, MAX_ENERGY)
			.withStyle(e > 0 ? ChatFormatting.AQUA : ChatFormatting.RED));
		if (e <= 0) {
			tooltip.accept(Component.translatable("tooltip.mechatech.exo_no_power").withStyle(ChatFormatting.RED));
		}
		ItemContainerContents modules = stack.getOrDefault(ModComponents.EXO_MODULES, ItemContainerContents.EMPTY);
		modules.nonEmptyItemCopyStream().forEach(m ->
			tooltip.accept(Component.literal(" ▪ ").append(m.getHoverName()).withStyle(ChatFormatting.DARK_AQUA)));
	}
}
