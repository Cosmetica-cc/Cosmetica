package com.eyezah.cosmetics.mixin;

import cc.cosmetica.api.Box;
import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.Model;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.ThreadPool;
import com.eyezah.cosmetics.cosmetics.Hats;
import com.eyezah.cosmetics.cosmetics.ShoulderBuddies;
import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.eyezah.cosmetics.cosmetics.model.Models;
import com.eyezah.cosmetics.cosmetics.model.OverriddenModel;
import com.eyezah.cosmetics.utils.Debug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@Inject(at = @At("HEAD"), method = "chat", cancellable = true)
	private void sendMessage(String string, CallbackInfo info) {
		if (Cosmetica.api == null) return; // no debug commands if offline

		if (!string.isEmpty() && string.charAt(0) == '/' && Debug.debugCommands()) {
			String[] args = string.split(" ");

			if (args[0].equals("/cosmetica")) {
				if (args.length == 1) {
					OverriddenModel.disableDebugModels();
					Minecraft.getInstance().gui.getChat().addMessage(new TranslatableComponent("cosmetica.debugCosmetica.disable"));
				} else if (args.length == 3) {
					// /cosmetica <type> <cosmeticid>
					String cosmeticType = args[1];
					String cosmeticId = Cosmetica.urlEncode(args[2]);

					if (cosmeticId.charAt(0) == '-' && "shoulderbuddy".equals(cosmeticType)) {
						ShoulderBuddies.overridden.setDebugModel(new BakableModel("-sheep", "Jeb_ Sheep", null, null, 0, new Box(0, 0, 0, 0, 0, 0)));
					} else {
						Cosmetica.runOffthread(() -> {
							var type = CosmeticType.fromUrlString(cosmeticType);

							if (type.isPresent()) {
								Cosmetica.api.getCosmetic(type.get(), cosmeticId).ifSuccessfulOrElse(cosmetic -> {
									if (cosmetic instanceof Model model) {
										if (cosmeticType.equals("hat")) {
											Hats.overridden.setDebugModel(Models.createBakableModel(model));
										}
										else if (cosmeticType.equals("shoulderbuddy")) {
											ShoulderBuddies.overridden.setDebugModel(Models.createBakableModel(model));
										}
									}
									else {
										Cosmetica.LOGGER.warn("Can't handle non-model cosmetics in /cosmetica <type> <id> currently.");
									}
								}, Cosmetica.logErr("Error recieving override cosmetic:"));
							}
						}, ThreadPool.GENERAL_THREADS);
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
					default:
						break;
					}
				}

				info.cancel();
			}
		}
	}
}
