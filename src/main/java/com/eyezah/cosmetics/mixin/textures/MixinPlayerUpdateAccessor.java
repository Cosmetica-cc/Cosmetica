package com.eyezah.cosmetics.mixin.textures;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundPlayerInfoPacket.PlayerUpdate.class)
public interface MixinPlayerUpdateAccessor {
	@Accessor
	void setProfile(GameProfile newProfile);
}
