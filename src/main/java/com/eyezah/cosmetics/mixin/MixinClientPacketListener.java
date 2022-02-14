package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Cosmetica;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
	@Shadow @Final private Minecraft minecraft;

	@Inject(at = @At("RETURN"), method = "handleAddPlayer")
	private void onHandleAddPlayer(ClientboundAddPlayerPacket packet, CallbackInfo ci) {
		Cosmetica.clearPlayerData(packet.getPlayerId());
	}

	@Inject(at = @At("RETURN"), method = "handleLogin")
	private void onHandleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
		Cosmetica.clearPlayerData(this.minecraft.player.getUUID());
	}
}
