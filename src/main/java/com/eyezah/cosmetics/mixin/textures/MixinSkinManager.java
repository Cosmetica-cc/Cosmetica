package com.eyezah.cosmetics.mixin.textures;

import java.io.File;

import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.minecraft.MinecraftSessionService;

import com.eyezah.cosmetics.SessionWrapperService;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;

@Mixin(SkinManager.class)
public class MixinSkinManager {
	@Shadow
	@Final
	@Mutable
	private MinecraftSessionService sessionService;

	@Inject(at = @At("RETURN"), method = "<init>")
	private void notLegallyQuestionableWeChecked(TextureManager textureManager, File file, MinecraftSessionService minecraftSessionService, CallbackInfo info) {
		if (!(this.sessionService instanceof SessionWrapperService)) {
			this.sessionService = new SessionWrapperService(this.sessionService);
		}
	}
}