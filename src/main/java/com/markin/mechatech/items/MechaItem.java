package com.markin.mechatech.items;

import java.util.function.Consumer;

import com.markin.mechatech.component.ModComponents;
import com.markin.mechatech.entity.MechaEntity;
import com.markin.mechatech.entity.ModEntities;
import com.markin.mechatech.mecha.MechaData;
import com.markin.mechatech.mecha.MechaPartData;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

/** Mecha empacotado: clique num bloco pra colocar o mecha no mundo. */
public class MechaItem extends Item {
	public MechaItem(Properties properties) {
		super(properties);
	}

	public static MechaData data(ItemStack stack) {
		return stack.getOrDefault(ModComponents.MECHA_DATA, MechaData.EMPTY);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		MechaData d = data(ctx.getItemInHand());
		if (!d.complete()) return InteractionResult.FAIL;
		if (ctx.getLevel() instanceof ServerLevel level) {
			BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
			MechaEntity mecha = ModEntities.MECHA.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
			if (mecha == null) return InteractionResult.FAIL;
			float yaw = ctx.getPlayer() != null ? ctx.getPlayer().getYRot() + 180f : 0f;
			mecha.setData(d);
			mecha.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
			mecha.yBodyRot = mecha.yHeadRot = yaw;
			if (!level.noCollision(mecha, mecha.getBoundingBox())) {
				if (ctx.getPlayer() != null) ctx.getPlayer().sendOverlayMessage(Component.translatable("message.mechatech.mecha_no_space"));
				return InteractionResult.FAIL;
			}
			level.addFreshEntity(mecha);
			level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.NEUTRAL, 1f, 0.6f);
			ctx.getItemInHand().shrink(1);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return data(stack).complete();
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		MechaData d = data(stack);
		return Math.round(13f * d.energy() / Math.max(1, d.maxEnergy()));
	}

	@Override
	public int getBarColor(ItemStack stack) {
		MechaData d = data(stack);
		float f = (float) d.energy() / Math.max(1, d.maxEnergy());
		return f > 0.25f ? 0x4FE8D6 : f > 0 ? 0xFFA030 : 0xFF4040;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
								Consumer<Component> tooltip, TooltipFlag flag) {
		MechaData d = data(stack);
		if (!d.complete()) return;
		tooltip.accept(Component.translatable("tooltip.mechatech.mecha_energy", d.energy(), d.maxEnergy()).withColor(0x7DF9E8));
		tooltip.accept(Component.translatable("tooltip.mechatech.mecha_size", String.format("%.1f", d.height()), d.scale()).withStyle(ChatFormatting.GRAY));
		for (MechaPartData p : d.parts()) {
			if (MechaData.present(p)) tooltip.accept(Component.literal(" • ").append(Component.translatable("part.mechatech." + p.variant()))
				.append(" " + p.scale() + "%").withStyle(ChatFormatting.DARK_AQUA));
		}
	}
}
