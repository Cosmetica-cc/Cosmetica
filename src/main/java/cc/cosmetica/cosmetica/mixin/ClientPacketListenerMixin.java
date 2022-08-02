package cc.cosmetica.cosmetica.mixin;

import cc.cosmetica.cosmetica.utils.Debug;
import cc.cosmetica.cosmetica.Cosmetica;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
	@Shadow @Final private Minecraft minecraft;

	@Inject(at = @At("RETURN"), method = "handleLogin")
	private void onHandleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
		String address = "fake server " + System.currentTimeMillis();
		if (Minecraft.getInstance().getCurrentServer() != null && !Objects.equals(Minecraft.getInstance().getCurrentServer().ip, Cosmetica.authServer)) address = Minecraft.getInstance().getCurrentServer().ip;
		if (Cosmetica.currentServerAddressCache.isEmpty() || !Objects.equals(Cosmetica.currentServerAddressCache, address)) {
			Cosmetica.currentServerAddressCache = address;
			Debug.info("Clearing all player data due to login.");
			Cosmetica.clearAllCaches();
			Cosmetica.getPlayerData(this.minecraft.player);
		}
	}
}
