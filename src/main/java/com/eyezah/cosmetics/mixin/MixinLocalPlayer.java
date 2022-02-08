package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.cosmetics.Hat;
import com.eyezah.cosmetics.cosmetics.ShoulderBuddy;
import com.eyezah.cosmetics.cosmetics.model.Models;
import com.eyezah.cosmetics.cosmetics.model.OverriddenModel;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {
	@Inject(at = @At("HEAD"), method = "chat", cancellable = true)
	private void sendMessage(String string, CallbackInfo info) {
		if (!string.isEmpty() && string.charAt(0) == '/' && Debug.debugCommands()) {
			String[] args = string.split(" ");

			if (args[0].equals("/cosmetica")) {
				if (args.length == 1) {
					OverriddenModel.disable();
					Minecraft.getInstance().gui.getChat().addMessage(new TranslatableComponent("cosmetica.debugCosmetica.disable"));
				} else if (args.length == 3) {
					String urlEncodedType = Cosmetica.urlEncode(args[1]);
					String urlEncodedCosmeticId = Cosmetica.urlEncode(args[2]);

					Cosmetica.runOffthread(() -> {
						String url = Cosmetica.apiServerHost + "/get/cosmetic?type=" + urlEncodedType + "&id=" + urlEncodedCosmeticId + "&timestamp=" + System.currentTimeMillis();
						Debug.info(url, "always_print_urls");

						try (Response response = Response.request(url)) {
							JsonObject json = response.getAsJson();

							if (!json.has("error")) {
								json.addProperty("id", urlEncodedCosmeticId);

								if (urlEncodedType.equals("hat")) {
									Hat.overridden.setReplacedModel(Models.createBakableModel(json));
								} else if (urlEncodedType.equals("shoulderbuddy")) {
									ShoulderBuddy.overridden.setReplacedModel(Models.createBakableModel(json));
								}
							}
						} catch (IOException e) {
							Cosmetica.LOGGER.error("Error recieving override cosmetic:");
							e.printStackTrace();
						}
					});
				}
			}

			info.cancel();
		}
	}
}
