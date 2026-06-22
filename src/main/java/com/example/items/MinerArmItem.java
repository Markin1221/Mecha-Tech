package com.example.items;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;


public class MinerArmItem extends Item {

	public MinerArmItem(Properties properties) {
		super(properties);
	}

	

@Override
public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay displayComponent, @NotNull Consumer<Component> textConsumer, @NotNull TooltipFlag type) {
	textConsumer.accept(Component.translatable("itemTooltip.modid.miner_arm").withStyle(ChatFormatting.GOLD));
}
}