package com.markin.mechatech.items;

import com.markin.mechatech.MechaTech;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

/** Chave Inglesa: clique direito gira blocos; agachado desmonta máquinas do mod. */
public class WrenchItem extends Item {

	public WrenchItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);

		// Agachado: desmonta máquinas do Mecha-Tech (dropa o bloco com os itens)
		if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()
			&& BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals(MechaTech.MOD_ID)) {
			if (!level.isClientSide()) {
				level.destroyBlock(pos, true, context.getPlayer());
			}
			return InteractionResult.SUCCESS;
		}

		BlockState rotated = state.rotate(Rotation.CLOCKWISE_90);

		if (rotated == state) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			level.setBlock(pos, rotated, Block.UPDATE_ALL);
			level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.6F, 1.6F);
			if (context.getPlayer() != null) {
				context.getItemInHand().hurtAndBreak(1, context.getPlayer(), context.getHand());
			}
		}
		return InteractionResult.SUCCESS;
	}
}
