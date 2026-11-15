package com.markin.mechatech.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.markin.mechatech.entity.MechaEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;

/** Dentro do mecha não aparece a mão do jogador em primeira pessoa (quem trabalha são os braços do mecha). */
@Mixin(ItemInHandRenderer.class)
public class HideMechaHandsMixin {
	@Inject(at = @At("HEAD"), method = "renderHandsWithItems", cancellable = true)
	private void mechatech$hideHands(float partialTick, PoseStack pose, SubmitNodeCollector collector, LocalPlayer player, int light, CallbackInfo ci) {
		if (player.getVehicle() instanceof MechaEntity) ci.cancel();
	}
}
