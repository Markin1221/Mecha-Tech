package com.markin.mechatech.exo;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class ExoModuleItem extends Item {
	public final ExoModule module;

	public ExoModuleItem(ExoModule module, Properties properties) {
		super(properties.stacksTo(1));
		this.module = module;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
								Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.translatable("tooltip.mechatech.module." + module.id).withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("tooltip.mechatech.module_piece",
			Component.translatable("tooltip.mechatech.piece." + module.piece.getName())).withStyle(ChatFormatting.DARK_AQUA));
		tooltip.accept(Component.translatable("tooltip.mechatech.module_cost", module.costPerSecond).withStyle(ChatFormatting.DARK_GRAY));
	}
}
