/*
 * Copyright 2022 EyezahMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.cosmetica.cosmetica.mixin;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.cosmetics.ShoulderBuddies;
import cc.cosmetica.cosmetica.utils.DebugMode;
import cc.cosmetica.cosmetica.cosmetics.model.Models;
import cc.cosmetica.cosmetica.utils.TextComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalInt;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@Inject(at = @At("HEAD"), method = "chat", cancellable = true)
	private void sendMessage(String string, CallbackInfo info) {
		if (!string.isEmpty() && string.charAt(0) == '/' && DebugMode.debugCommands()) {
			String[] args = string.split(" ");

			if (args[0].equals("/cosmetica")) {
				if (args.length == 2) { // cache commands
					switch (args[1]) {
					case "infocache":
						Minecraft.getInstance().gui.getChat().addMessage(TextComponents.literal(Cosmetica.getCachedPlayers().toString()));
						break;
					case "modelcache":
						Minecraft.getInstance().gui.getChat().addMessage(TextComponents.literal(Models.getCachedModels().toString()));
						break;
					default:
						break;
					}
				}
				else if (args.length == 3) {
					if (args[1].equals("staticsb")) {
						if (args[2].equals("true")) {
							ShoulderBuddies.staticOverride = OptionalInt.of(1);
						}
						else if (args[2].equals("false")) {
							ShoulderBuddies.staticOverride = OptionalInt.of(0);
						}
						else {
							ShoulderBuddies.staticOverride = OptionalInt.empty();
						}
					}
				}

				info.cancel();
			}
		}
	}
}
