package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Authentication;
import com.eyezah.cosmetics.Cosmetics;
import com.eyezah.cosmetics.screens.UnauthenticatedScreen;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.eyezah.cosmetics.Cosmetics.*;

@Mixin(ConnectScreen.class)
public class MixinConnectScreen {

	@Shadow static AtomicInteger UNIQUE_THREAD_ID;
	@Shadow static Logger LOGGER;
	@Shadow boolean aborted;
	@Shadow Connection connection;
	@Shadow Screen parent;


	/**
	 * @author eyezah
	 * @reason cos i can lol
	 */
	@Overwrite
	public static void startConnecting(Screen screen, Minecraft minecraft, ServerAddress serverAddress, @Nullable ServerData serverData) {
		boolean isAuth = false;
		if (serverAddress.getHost().equals("localhost") && serverAddress.getPort() == 25565) {
			System.out.println("connection to localhost :)");
			isAuth = true;
		}
		System.out.println("started connecting :))");
		connectScreen = MixinConnectScreenInvoker.getConnectScreen(screen);
		if (!isAuth) {
			minecraft.clearLevel();
			minecraft.setCurrentServer(serverData);
			minecraft.setScreen(connectScreen);
		} else {
			minecraft.setCurrentServer(serverData);
		}
		((MixinConnectScreenInvoker) connectScreen).connectScreenConnect(minecraft, serverAddress);
	}

	/**
	 * @author eyezah
	 * @reason	cos i can lol
	 */
	@Overwrite
	public void connect(Minecraft minecraft, ServerAddress serverAddress) {
		boolean isAuth = false;
		if (serverAddress.getHost().equals(authServerHost) && serverAddress.getPort() == authServerPort) {
			//System.out.println("connection to localhost :)");
			isAuth = true;
		} else {
			//System.out.println("not localhost");
		}
		boolean finalIsAuth2 = isAuth;
		Thread thread = new Thread("Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()) {
			public void run() {
				InetSocketAddress inetSocketAddress = null;

				try {
					if (aborted) {
						System.out.println("aborted");
						return;
					}

					Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT.resolveAddress(serverAddress).map(ResolvedServerAddress::asInetSocketAddress);
					if (aborted) {
						return;
					}

					if (!optional.isPresent()) {
						if (!finalIsAuth2) {
							minecraft.execute(() -> {
								minecraft.setScreen(new DisconnectedScreen(parent, CommonComponents.CONNECT_FAILED, ConnectScreen.UNKNOWN_HOST_MESSAGE));
							});
						} else{
							minecraft.execute(() -> {
								minecraft.setScreen(new UnauthenticatedScreen(parent, Cosmetics.optionsStorage, false));
							});
						}
						return;
					}

					inetSocketAddress = (InetSocketAddress) optional.get();
					connection = Connection.connectToServer(inetSocketAddress, minecraft.options.useNativeTransport());
					connection.setListener(new ClientHandshakePacketListenerImpl(connection, minecraft, parent, ((MixinConnectScreenInvoker) connectScreen)::doUpdateStatus));
					connection.send(new ClientIntentionPacket(inetSocketAddress.getHostName(), inetSocketAddress.getPort(), ConnectionProtocol.LOGIN));
					connection.send(new ServerboundHelloPacket(minecraft.getUser().getGameProfile()));
				} catch (Exception var4) {
					if (aborted) {
						System.out.println("aborted");
						return;
					}

					LOGGER.error((String) "Couldn't connect to server", (Throwable) var4);
					String string = inetSocketAddress == null ? var4.toString() : var4.toString().replaceAll(inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort(), "");
					System.out.println(string);
					if (!finalIsAuth2) {
						minecraft.execute(() -> {
							minecraft.setScreen(new DisconnectedScreen(parent, CommonComponents.CONNECT_FAILED, new TranslatableComponent("disconnect.genericReason", new Object[]{string})));
						});
					} else {
						minecraft.execute(() -> {
							minecraft.setScreen(new UnauthenticatedScreen(parent, Cosmetics.optionsStorage, false));
						});
					}
				}

			}
		};
		thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
		thread.start();
	}
}