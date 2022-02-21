package com.eyezah.cosmetics.mixin.textures;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.CosmeticaSkinManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.Response;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URL;

@Mixin(value = YggdrasilMinecraftSessionService.class, remap = false)
public class MixinYggdrasilSessionService {
	/**
	 * @reason use cosmetica skin servers to get custom cape and stuff
	 */
	@Redirect(
			method = "fillGameProfile",
			at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;makeRequest(Ljava/net/URL;Ljava/lang/Object;Ljava/lang/Class;)Lcom/mojang/authlib/yggdrasil/response/Response;")
	)
	private <T extends Response> T fillCosmeticaProfileProperties(YggdrasilAuthenticationService instance, URL url, Object input, Class<T> classOfT,
																  GameProfile profile, boolean requireSecure) throws AuthenticationException {
		return ((MixinYggdrasilAuthenticationServiceInvoker) instance).invokeMakeRequest(CosmeticaSkinManager.getCosmeticaURL(url, profile, requireSecure), input, classOfT);
	}

	@Inject(at = @At("HEAD"), method = "isAllowedTextureDomain", cancellable = true)
	private static void onIsAllowedTextureDomain(String uri, CallbackInfoReturnable<Boolean> info) {
		if (uri.contains(Cosmetica.apiServerHost.substring(8))) {
			info.setReturnValue(true); // yes use cosmetica textures
		}
	}
}
