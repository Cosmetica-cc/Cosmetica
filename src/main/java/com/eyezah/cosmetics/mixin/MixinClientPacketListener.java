package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.utils.Debug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow @Nullable public abstract PlayerInfo getPlayerInfo(UUID uniqueId);

	@Shadow public abstract @Nullable PlayerInfo getPlayerInfo(String name);

	@Inject(at = @At("HEAD"), method = "handlePlayerInfo")
	private void onHandlePlayerInfo(ClientboundPlayerInfoPacket packet, CallbackInfo ci) {
		if (this.minecraft.isSameThread()) {
			if (packet.getAction() == ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER) {
				for (var spain : packet.getEntries()) {
					UUID uuid = spain.getProfile().getId();
					String name = "missingno";

					try {
						name = this.getPlayerInfo(uuid).getProfile().getName();
					} catch (Exception e) {
					}

					Debug.info("Clearing player data of {} (UUID {})", name, uuid);
					Cosmetica.clearPlayerData(spain.getProfile().getId());
				}
			}
		}
	}

	@Inject(at = @At("RETURN"), method = "handleLogin")
	private void onHandleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
		Debug.info("Clearing player data due to login.");
		Cosmetica.clearPlayerData(this.minecraft.player.getUUID());
	}
}
