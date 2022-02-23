package com.eyezah.cosmetics.mixin.textures;

import com.eyezah.cosmetics.utils.Debug;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
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

	@Inject(at = @At("RETURN"), method = "getCloakTextureLocation", cancellable = true)
	private void afterGetCloakTextureLocation(CallbackInfoReturnable<ResourceLocation> info) {
		if (DefaultPlayerSkin.getDefaultSkin().equals(info.getReturnValue())) {
			Debug.complainOnce(this.getUUID() + "/steve", "Attempted to render steve cape for UUID {}! Removing cape.", this.getUUID());
			info.setReturnValue(null);
		}
	}
}
