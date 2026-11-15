package com.markin.mechatech.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.markin.mechatech.entity.MechaEntity;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;

/** Esconde o piloto (e a armadura dele) enquanto está dentro do mecha. */
@Mixin(EntityRenderDispatcher.class)
public class HideMechaPilotMixin {
	@Inject(at = @At("HEAD"), method = "shouldRender", cancellable = true)
	private <E extends Entity> void mechatech$hidePilot(E entity, Frustum frustum, double x, double y, double z,
														CallbackInfoReturnable<Boolean> cir) {
		if (entity.getVehicle() instanceof MechaEntity) cir.setReturnValue(false);
	}
}
