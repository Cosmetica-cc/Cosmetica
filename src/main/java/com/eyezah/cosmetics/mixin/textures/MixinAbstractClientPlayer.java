package com.eyezah.cosmetics.mixin.textures;

import com.eyezah.cosmetics.CosmeticaSkinManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends Player {
	public MixinAbstractClientPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
		super(level, blockPos, f, gameProfile);
	}

	@Inject(at = @At("RETURN"), method = "isCapeLoaded", cancellable = true)
	private void isCosmeticaCapeLoaded(CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(info.getReturnValueZ() && CosmeticaSkinManager.isPlayerCapeLoaded(this.getUUID()));
	}

	@Inject(at = @At("RETURN"), method = "getCloakTextureLocation", cancellable = true)
	private void removeSteve(CallbackInfoReturnable<ResourceLocation> info) {
		ResourceLocation rl = info.getReturnValue();

		if (rl != null && Minecraft.getInstance().getTextureManager().getTexture(rl) instanceof HttpTexture texture) {
			if (!((MixinHttpTextureAccessor)texture).isUploaded()) {
				info.setReturnValue(null);
			}
		}
	}
}
