package com.markin.mechatech.mecha;

import java.util.function.Consumer;

import com.markin.mechatech.component.ModComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Item de peça de mecha. A variante e a escala ficam no componente MECHA_PART. */
public class MechaPartItem extends Item {
	public final PartType type;

	public MechaPartItem(PartType type, Properties properties) {
		super(properties.stacksTo(1));
		this.type = type;
	}

	@Override
	public Component getName(ItemStack stack) {
		MechaPartData data = stack.get(ModComponents.MECHA_PART);
		if (data != null && data.def() != null) {
			return Component.translatable("part.mechatech." + data.variant());
		}
		return super.getName(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
								Consumer<Component> tooltip, TooltipFlag flag) {
		MechaPartData d = stack.get(ModComponents.MECHA_PART);
		if (d == null || d.def() == null) {
			tooltip.accept(Component.translatable("tooltip.mechatech.part_blank").withStyle(ChatFormatting.GRAY));
			return;
		}
		tooltip.accept(Component.translatable("tooltip.mechatech.part_scale", d.scale()).withStyle(ChatFormatting.DARK_AQUA));
		line(tooltip, "weight", d.weight(), ChatFormatting.GRAY);
		if (d.armor() > 0) line(tooltip, "armor", d.armor(), ChatFormatting.BLUE);
		if (d.power() > 0) line(tooltip, "power", d.power(), ChatFormatting.RED);
		if (d.speed() > 0) line(tooltip, "speed", d.speed(), ChatFormatting.GREEN);
		if (d.capacity() > 0) line(tooltip, "capacity", d.capacity(), ChatFormatting.GOLD);
		if (d.energy() > 0) line(tooltip, "energy", d.energy(), ChatFormatting.AQUA);
	}

	private static void line(Consumer<Component> tooltip, String key, int value, ChatFormatting color) {
		tooltip.accept(Component.translatable("stat.mechatech." + key, value).withStyle(color));
	}
}
