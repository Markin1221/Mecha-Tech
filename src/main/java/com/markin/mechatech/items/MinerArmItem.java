package com.markin.mechatech.items;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;


public class MinerArmItem extends Item {

    public MinerArmItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        TooltipContext context,
        TooltipDisplay display,
        Consumer<Component> consumer,
        TooltipFlag flag
    ) {
        consumer.accept(
            Component.literal("Braço Minerador")
                .withStyle(ChatFormatting.GOLD)
        );
    }
}