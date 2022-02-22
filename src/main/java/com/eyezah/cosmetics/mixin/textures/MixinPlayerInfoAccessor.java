package com.eyezah.cosmetics.mixin.textures;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInfo.class)
public interface MixinPlayerInfoAccessor {
	@Accessor
	@Mutable
	void setProfile(GameProfile newProfile);
}
