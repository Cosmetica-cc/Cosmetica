package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Cosmetica;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
	@Inject(at = @At("HEAD"), method = "isEntityUpsideDown", cancellable = true)
	private static void alsoAustralians(LivingEntity entity, CallbackInfoReturnable<Boolean> info) {
		if (entity instanceof Player player && Cosmetica.shouldRenderUpsideDown(player)) {
			info.setReturnValue(true);
		}
	}

	@Inject(
			method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z",
			at = @At("HEAD"),
			cancellable = true
	)
	private void shouldShowName(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
		if (Cosmetica.getConfig().shouldShowNametagInThirdPerson() && entity == Minecraft.getInstance().getCameraEntity()) cir.setReturnValue(Minecraft.renderNames());
	}
}
