package com.eyezah.cosmetics.mixin.textures;

import com.eyezah.cosmetics.CosmeticaSkinManager;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientboundPlayerInfoPacket.class)
public class MixinClientboundPlayerInfoPacket {
	@Shadow @Final private List<ClientboundPlayerInfoPacket.PlayerUpdate> entries;

	@Shadow @Final private ClientboundPlayerInfoPacket.Action action;

	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V")
	private void afterConstructClient(FriendlyByteBuf friendlyByteBuf, CallbackInfo info) {
		if (Minecraft.getInstance().getMinecraftSessionService() instanceof YggdrasilMinecraftSessionService ygg) {
			MixinYggdrasilAuthenticationServiceInvoker yggi = (MixinYggdrasilAuthenticationServiceInvoker) ygg.getAuthenticationService();

			if (this.action == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
				CosmeticaSkinManager.modifyServerGameProfiles(this.entries, yggi);
			}
		}
	}
}
