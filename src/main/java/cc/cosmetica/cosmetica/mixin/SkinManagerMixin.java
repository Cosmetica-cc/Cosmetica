package cc.cosmetica.cosmetica.mixin;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.ThreadPool;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(SkinManager.class)
public class SkinManagerMixin {
	// See comment in Cosmetica.forwardPublicUserInfoToNametag
	@Inject(at = @At("RETURN"), method = "registerTextures")
	public void beforeRegisterTextures(GameProfile profile, SkinManager.TextureInfo textureInfo, CallbackInfoReturnable<CompletableFuture<PlayerSkin>> info) {
		Cosmetica.runOffthread(() -> Cosmetica.forwardPublicUserInfoToNametag(profile), ThreadPool.GENERAL_THREADS);
	}
}
