package com.eyezah.cosmetics.mixin.textures;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.CosmeticaSkinManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
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

	@Inject(at = @At("HEAD"), method = "isCapeLoaded", cancellable = true)
	private void isCosmeticaCapeLoaded(CallbackInfoReturnable<Boolean> info) {
		if (!Cosmetica.isProbablyNPC(this.uuid)) info.setReturnValue(Cosmetica.isPlayerCached(this.uuid));
	}

	@Inject(at = @At("HEAD"), method = "getCloakTextureLocation", cancellable = true)
	private void addCosmeticaCapes(CallbackInfoReturnable<ResourceLocation> info) {
		if (!Cosmetica.isProbablyNPC(this.uuid)) { // ignore npcs
			ResourceLocation location = Cosmetica.isPlayerCached(this.uuid) ? Cosmetica.getPlayerData(this).cape() : null; // get the location if cached
			if (location != null && !CosmeticaSkinManager.isUploaded(location)) location = null; // only actually get it if it's been uploaded
			info.setReturnValue(location); // set the return value to our one
		}
	}
}
