package com.eyezah.cosmetics.mixin.screen;

import com.eyezah.cosmetics.Authentication;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.screens.LoadingScreen;
import com.eyezah.cosmetics.utils.AuthenticatingScreen;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.eyezah.cosmetics.Cosmetica.*;

@Mixin(ConnectScreen.class)
public class MixinConnectScreen implements AuthenticatingScreen {

	@Shadow static AtomicInteger UNIQUE_THREAD_ID;
	@Shadow static Logger LOGGER;
	@Shadow boolean aborted;
	@Shadow Connection connection;
	@Shadow Screen parent;

	@Inject(at = @At("HEAD"), method = "startConnecting", cancellable = true)
	private static void startConnecting(Screen screen, Minecraft minecraft, ServerAddress serverAddress, @Nullable ServerData serverData, CallbackInfo info) {
		if (serverAddress.getHost().equals(authServerHost) && serverAddress.getPort() == authServerPort) {
			if (!Authentication.currentlyAuthenticating && (minecraft.screen instanceof JoinMultiplayerScreen || minecraft.screen instanceof DirectJoinServerScreen)) {
				minecraft.setScreen(new LoadingScreen(minecraft.screen, minecraft.options));
			} else {
				connectScreen = MixinConnectScreenInvoker.getConnectScreen(screen);
				minecraft.setCurrentServer(serverData);
				((AuthenticatingScreen) connectScreen).eyezahAuthConnect(minecraft, serverAddress);
			}

			info.cancel();
		}
	}

	/**
	 * @author eyezah
	 */
	@Override
	public void eyezahAuthConnect(Minecraft minecraft, ServerAddress serverAddress) {
		Thread thread = new Thread("Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()) {
			public void run() {
				InetSocketAddress inetSocketAddress = null;

				try {
					if (aborted) {
						Cosmetica.LOGGER.warn("aborted");
						return;
					}

					Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT.resolveAddress(serverAddress).map(ResolvedServerAddress::asInetSocketAddress);
					if (aborted) {
						return;
					}

					if (!optional.isPresent()) {
						Authentication.showUnauthenticatedIfLoading();
						return;
					}

					inetSocketAddress = optional.get();
					connection = Connection.connectToServer(inetSocketAddress, minecraft.options.useNativeTransport());
					connection.setListener(new ClientHandshakePacketListenerImpl(connection, minecraft, parent, ((MixinConnectScreenInvoker) connectScreen)::doUpdateStatus));
					connection.send(new ClientIntentionPacket(inetSocketAddress.getHostName(), inetSocketAddress.getPort(), ConnectionProtocol.LOGIN));
					connection.send(new ServerboundHelloPacket(minecraft.getUser().getGameProfile()));
				} catch (Exception e) {
					if (aborted) {
						Cosmetica.LOGGER.warn("aborted");
						return;
					}

					LOGGER.error("Couldn't connect to cosmetica auth server", e);

					Authentication.showUnauthenticatedIfLoading();
				}
			}
		};
		thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
		thread.start();
	}
}