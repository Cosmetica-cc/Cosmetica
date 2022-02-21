package com.eyezah.cosmetics.mixin.textures;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.response.Response;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.net.URL;

@Mixin(value = YggdrasilAuthenticationService.class, remap = false)
public interface MixinYggdrasilAuthenticationServiceInvoker {
	@Invoker
	<T extends Response> T invokeMakeRequest(URL url, Object obj, Class<T> resultClass) throws AuthenticationException;
}
