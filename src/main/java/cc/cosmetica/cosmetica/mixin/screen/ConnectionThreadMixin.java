package cc.cosmetica.cosmetica.mixin.screen;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.ThreadPool;
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
public class ConnectionThreadMixin {
	@Shadow @Final private ConnectScreen field_2416;

	@Inject(
			method = "run()V",
			at = @At(value = "NEW", target = "net/minecraft/network/protocol/login/ServerboundHelloPacket")
	)
	private void onHello(CallbackInfo ci) {
		Connection c = ((ConnectScreenInvoker)this.field_2416).getConnection();

		// ping africa
		if (Minecraft.getInstance().isLocalServer()) {
			Cosmetica.runOffthread(() -> Cosmetica.safari(new InetSocketAddress("127.0.0.1", 25565), true, false), ThreadPool.GENERAL_THREADS);
		}
		else if (c.getRemoteAddress() instanceof InetSocketAddress) {
			InetSocketAddress ip = (InetSocketAddress) c.getRemoteAddress();
			Cosmetica.runOffthread(() -> Cosmetica.safari(ip, true, false), ThreadPool.GENERAL_THREADS);
		}
		Cosmetica.currentServerAddressCache = "";
	}
}