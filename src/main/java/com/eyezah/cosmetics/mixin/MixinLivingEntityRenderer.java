package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Cosmetica;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {
	@Inject(at = @At("HEAD"), method = "isEntityUpsideDown", cancellable = true)
	private static void alsoAustralians(LivingEntity entity, CallbackInfoReturnable<Boolean> info) {
		if (entity instanceof Player player && Cosmetica.shouldRenderUpsideDown(player)) {
			info.setReturnValue(true);
		}
	}
}
