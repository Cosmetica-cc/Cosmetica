package com.eyezah.cosmetics;

import com.eyezah.cosmetics.screens.LoadingScreen;
import com.eyezah.cosmetics.screens.MainScreen;
import com.eyezah.cosmetics.screens.UnauthenticatedScreen;
import com.eyezah.cosmetics.screens.UpdatingSettings;
import com.eyezah.cosmetics.utils.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.eyezah.cosmetics.Cosmetics.*;

public class Authentication {
	private static boolean currentlyAuthenticated = false;
	private static String token = "";
	public static boolean currentlyAuthenticating = false;

	public static boolean isCurrentlyAuthenticated() {
		return currentlyAuthenticated;
	}

	public static String getToken() {
		return token;
	}

	private static void syncSettings() {
		Thread requestThread = new Thread(() -> {
			if (token.equals("")) {
				runAuthentication(Minecraft.getInstance().screen);
				return;
			}
			try (Response response = Response.request("https://eyezah.com/cosmetics/api/get/settings?token=\" + token")) {
				JsonObject jsonObject = response.getAsJson();
				if (jsonObject.has("error")) {
					System.out.println("error: " + jsonObject.get("error").getAsString());
					if (Minecraft.getInstance().screen instanceof LoadingScreen || Minecraft.getInstance().screen instanceof UpdatingSettings) Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(new OptionsScreen(new TitleScreen(), optionsStorage), optionsStorage, false)));
					return;
				}
				regionSpecificEffects = jsonObject.get("per-region effects").getAsBoolean();
				doShoulderBuddies = jsonObject.get("do shoulder buddies").getAsBoolean();
				if (Minecraft.getInstance().screen instanceof LoadingScreen || Minecraft.getInstance().screen instanceof UpdatingSettings) {
					Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new MainScreen(screenStorage, optionsStorage)));
				}
			} catch (IOException e) {
				e.printStackTrace();
				token = "";
				if (Minecraft.getInstance().screen instanceof LoadingScreen || Minecraft.getInstance().screen instanceof UpdatingSettings) {
					Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(new OptionsScreen(new TitleScreen(), optionsStorage), optionsStorage, false)));
				}
				runAuthentication(Minecraft.getInstance().screen);
			}
		});
		requestThread.start();
	}

	public static void setToken(String testToken) {
		Thread requestThread = new Thread(() -> {
			try (Response response = Response.request("https://eyezah.com/cosmetics/api/client/verifyauthtoken?token=" + testToken + "&uuid=" + Minecraft.getInstance().getUser().getUuid() + "&access-token=" + Minecraft.getInstance().getUser().getAccessToken())) {
				String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8).trim();
				if (responseBody.startsWith("token:")) {
					token = responseBody.substring(6);
					currentlyAuthenticated = true;
					currentlyAuthenticating = false;
					syncSettings();
				} else {
					currentlyAuthenticating = false;
					if (Minecraft.getInstance().screen instanceof LoadingScreen) {
						Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(new OptionsScreen(new TitleScreen(), optionsStorage), optionsStorage, false)));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				currentlyAuthenticating = false;
				if (Minecraft.getInstance().screen instanceof LoadingScreen) {
					Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(new OptionsScreen(new TitleScreen(), optionsStorage), optionsStorage, false)));
				}
			}
		});
		requestThread.start();
	}

	public static void runAuthentication(Screen screen) {
		if (token.equals("")) {
			if (!currentlyAuthenticating) {
				currentlyAuthenticating = true;
				ConnectScreen.startConnecting(screen, Minecraft.getInstance(), new ServerAddress(authServerHost, authServerPort), new ServerData("Authentication Server", authServerHost + ":" + authServerPort, false));
			}
		} else {
			syncSettings();
		}
	}

	protected static void runAuthenticationCheckThread() {
		Thread requestThread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(1000 * 60 * 5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				syncSettings();
			}
		});
		requestThread.start();
	}
}
