package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.CosmeticaSkinManager;
import com.eyezah.cosmetics.ThreadPool;
import com.eyezah.cosmetics.cosmetics.Hat;
import com.eyezah.cosmetics.cosmetics.ShoulderBuddy;
import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.eyezah.cosmetics.cosmetics.model.Models;
import com.eyezah.cosmetics.cosmetics.model.OverriddenModel;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.UUID;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {
	@Inject(at = @At("HEAD"), method = "chat", cancellable = true)
	private void sendMessage(String string, CallbackInfo info) {
		if (!string.isEmpty() && string.charAt(0) == '/' && Debug.debugCommands()) {
			String[] args = string.split(" ");

			if (args[0].equals("/cosmetica")) {
				if (args.length == 1) {
					OverriddenModel.disableDebugModels();
					Minecraft.getInstance().gui.getChat().addMessage(new TranslatableComponent("cosmetica.debugCosmetica.disable"));
				} else if (args.length == 3) {
					if ("profilecache".equals(args[1])) { // /cosmetica profilecache <uuid>
						try {
							for (String infoLine : CosmeticaSkinManager.getCacheData(UUID.fromString(args[2]))) {
								Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(infoLine));
							}
						} catch (IllegalArgumentException e) {
							Cosmetica.LOGGER.error("Error executing debug command", e);
							info.cancel();
						}
					}

					// /cosmetica <type> <cosmeticid>
					String urlEncodedType = Cosmetica.urlEncode(args[1]);
					String urlEncodedCosmeticId = Cosmetica.urlEncode(args[2]);

					if (urlEncodedCosmeticId.charAt(0) == '-' && "shoulderbuddy".equals(urlEncodedType)) {
						ShoulderBuddy.overridden.setDebugModel(new BakableModel("-sheep", null, null, 0, JsonParser.parseString("[[0, 0, 0], [0, 0, 0]]").getAsJsonArray()));
					} else {
						Cosmetica.runOffthread(() -> {
							String url = Cosmetica.apiServerHost + "/get/cosmetic?type=" + urlEncodedType + "&id=" + urlEncodedCosmeticId + "&timestamp=" + System.currentTimeMillis();
							Debug.checkedInfo(url, "always_print_urls");

							try (Response response = Response.request(url)) {
								JsonObject json = response.getAsJson();

								if (!json.has("error")) {
									json.addProperty("id", urlEncodedCosmeticId);

									if (urlEncodedType.equals("hat")) {
										Hat.overridden.setDebugModel(Models.createBakableModel(json));
									} else if (urlEncodedType.equals("shoulderbuddy")) {
										ShoulderBuddy.overridden.setDebugModel(Models.createBakableModel(json));
									}
								}
							} catch (IOException e) {
								Cosmetica.LOGGER.error("Error recieving override cosmetic:");
								e.printStackTrace();
							}
						}, ThreadPool.LOOKUP_THREADS);
					}
				} else if (args.length == 2) { // cache commands
					switch (args[1]) {
					case "texcache":
						Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(Models.TEXTURE_MANAGER.toString()));
						break;
					case "infocache":
						Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(Cosmetica.getCachedPlayers().toString()));
						break;
					case "modelcache":
						Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(Models.getCachedModels().toString()));
						break;
					case "profilecache":
						for (String infoLine : CosmeticaSkinManager.getCacheData()) {
							Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(infoLine));
						}
						break;
					default:
						break;
					}
				}

				info.cancel();
			}
		}
	}
}
