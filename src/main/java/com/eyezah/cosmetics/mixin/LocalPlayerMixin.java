package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.cosmetics.model.Models;
import com.eyezah.cosmetics.utils.Debug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
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
				if (args.length == 2) { // cache commands
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
