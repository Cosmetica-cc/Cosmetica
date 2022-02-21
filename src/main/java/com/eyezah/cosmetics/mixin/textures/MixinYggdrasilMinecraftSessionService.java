package com.eyezah.cosmetics.mixin.textures;

import com.eyezah.cosmetics.CosmeticaSkinManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.Response;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.URL;

@Mixin(YggdrasilMinecraftSessionService.class)
public class MixinYggdrasilMinecraftSessionService {
	@Redirect(
			method = "fillGameProfile",
			at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;makeRequest(Ljava/net/URL;Ljava/lang/Object;Ljava/lang/Class;)Lcom/mojang/authlib/yggdrasil/response/Response;")
	)
	private <T extends Response> T modifyURL(YggdrasilAuthenticationService instance, URL url, Object input, Class<T> classOfT, GameProfile profile, boolean requiresSecure) {
		return ((MixinYggdrasilAuthenticationServiceInvoker) instance).invokeMakeRequest(
				CosmeticaSkinManager.getCosmeticaURL(profile, requiresSecure, url),
				input,
				classOfT
		);
	}
}
