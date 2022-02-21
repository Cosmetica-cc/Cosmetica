package com.eyezah.cosmetics.mixin.textures;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.authlib.yggdrasil.response.Response;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.net.URL;

@Mixin(YggdrasilAuthenticationService.class)
public interface MixinYggdrasilAuthenticationServiceInvoker {
	@Invoker
	<T extends Response> T invokeMakeRequest(URL url, Object object, Class<T> tClass);
}
