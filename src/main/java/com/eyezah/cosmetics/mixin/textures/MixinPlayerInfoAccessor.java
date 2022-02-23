package com.eyezah.cosmetics.mixin.textures;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(PlayerInfo.class)
public interface MixinPlayerInfoAccessor {
	@Accessor
	@Mutable
	void setProfile(GameProfile newProfile);

	@Invoker
	void invokeRegisterTextures();

	@Accessor
	void setPendingTextures(boolean pendingTextures);

	@Accessor
	Map<MinecraftProfileTexture.Type, ResourceLocation> getTextureLocations();
}
