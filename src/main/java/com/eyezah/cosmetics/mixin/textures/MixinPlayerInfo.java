package com.eyezah.cosmetics.mixin.textures;

import com.eyezah.cosmetics.CosmeticaSkinManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInfo.class)
public class MixinPlayerInfo {
	@Shadow @Final private GameProfile profile;

	@Inject(at = @At("RETURN"), method = "<init>")
	private void onConstructInfo(ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate, CallbackInfo ci) {
		if (Minecraft.getInstance().getMinecraftSessionService() instanceof YggdrasilMinecraftSessionService ygg) {
			CosmeticaSkinManager.modifyServerGameProfiles((PlayerInfo) (Object) this, this.profile, ygg);
		}
	}

	@ModifyArg(
			method = "registerTextures",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/SkinManager;registerSkins(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinTextureCallback;Z)V"),
			index = 2
	)
	private boolean e(boolean requireSecure) {
		return false;
	}

	@Inject(at = @At("RETURN"), method = "method_2956")
	private void onSkinTextureCallback(MinecraftProfileTexture.Type type, ResourceLocation location, MinecraftProfileTexture texture, CallbackInfo info) {
		CosmeticaSkinManager.markPlayerLoaded(this.profile.getId());
	}
}
