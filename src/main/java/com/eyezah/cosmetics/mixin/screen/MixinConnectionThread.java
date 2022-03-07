package com.eyezah.cosmetics.mixin.screen;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.ThreadPool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

@Mixin(targets = "net/minecraft/client/gui/screens/ConnectScreen$1")
public class MixinConnectionThread {
	@Shadow @Final private ConnectScreen field_2416;

	@Inject(
			method = "run()V",
			at = @At(value = "NEW", target = "net/minecraft/network/protocol/login/ServerboundHelloPacket")
	)
	private void onHello(CallbackInfo ci) {
		Connection c = ((MixinConnectScreenInvoker)this.field_2416).getConnection();

		// ping africa

		if (Minecraft.getInstance().isLocalServer()) {
			Cosmetica.runOffthread(() -> Cosmetica.safari(new InetSocketAddress("127.0.0.1", 25565), true), ThreadPool.GENERAL_THREADS);
		}
		else if (c.getRemoteAddress() instanceof InetSocketAddress ip) {
			Cosmetica.runOffthread(() -> Cosmetica.safari(ip, true), ThreadPool.GENERAL_THREADS);
		}
	}
}