package cc.cosmetica.cosmetica.mixin;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.ThreadPool;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkinManager.class)
public class SkinManagerMixin {
	@Inject(at = @At("HEAD"), method = "registerSkins")
	public void registerSkins(GameProfile profile, SkinManager.SkinTextureCallback callback, boolean bl, CallbackInfo info) {
		// See comment in the called method
		Cosmetica.runOffthread(() -> Cosmetica.forwardPublicUserInfoToNametag(profile), ThreadPool.GENERAL_THREADS);
	}
}
