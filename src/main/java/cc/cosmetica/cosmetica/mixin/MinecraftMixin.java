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

import cc.cosmetica.api.User;
import cc.cosmetica.cosmetica.Authentication;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.screens.*;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.utils.DebugMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 69) // i must be the first
public abstract class MinecraftMixin {
	@Shadow public abstract void setScreen(@Nullable Screen screen);

	@Shadow @Nullable public Screen screen;

	@Shadow @Final public Gui gui;

	@Shadow @Nullable public ClientLevel level;

	@Shadow @Nullable public LocalPlayer player;

	@Shadow
	public static Minecraft getInstance() {
		return null;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;shutdownExecutors()V"), method = "close")
	private void onClose(CallbackInfo info) {
		Cosmetica.onShutdownClient();
	}

	@Inject(at = @At("HEAD"), method = "setScreen", cancellable = true)
	private void addRegionSpecificEffectsPrompt(Screen screen, CallbackInfo info) {
		// if the RSE warning screen should appear cancel the current screen set in favour of a wrapper thereof
		if (RSEWarningScreen.appearNextScreenChange && !WelcomeScreen.isInTutorial) {
			RSEWarningScreen.appearNextScreenChange = false;
			this.setScreen(new RSEWarningScreen(screen));
			info.cancel();
		}
	}

	@Inject(at = @At("RETURN"), method = "setScreen", cancellable = true)
	private void addOnOpen(Screen screen, CallbackInfo info) {
		if (this.screen instanceof PlayerRenderScreen prs) {
			prs.onOpen();
		}
	}

	@Inject(at = @At("HEAD"), method = "setLevel")
	private void maybeClearCosmetics(ClientLevel level, CallbackInfo info) {
		if (PlayerData.getCacheSize() > 1024) {
			DebugMode.log("Clearing Cosmetica Caches");
			Cosmetica.clearAllCaches();
		}

		// also do the check thing
		if (Cosmetica.displayNext != null) {
			this.gui.getChat().addMessage(Cosmetica.displayNext);
			Cosmetica.displayNext = null;
		}
	}

	@Inject(at = @At("RETURN"), method = "tick")
	public void afterTick(CallbackInfo ci) {
		if (Cosmetica.openCustomiseScreen.consumeClick()) {
			if (this.screen == null) {
				this.setScreen(new LoadingScreen(null, Minecraft.getInstance().options, 1));
			}
			else if (this.screen instanceof CustomiseCosmeticsScreen ccs && ccs.canCloseWithBn()) {
				ccs.onClose();
			}
		}

		if (Cosmetica.snipe.consumeClick() && this.screen == null && Cosmetica.farPickPlayer != null) {
			DebugMode.log("Sniping Player: " + Cosmetica.farPickPlayer.getUUID());
			Authentication.snipedPlayer = new User(Cosmetica.farPickPlayer.getUUID(), Cosmetica.farPickPlayer.getName().getString());

			if (Authentication.hasSavedSettings()
					&& PlayerData.has(this.player.getUUID()) && PlayerData.has(Cosmetica.farPickPlayer.getUUID())) {
				PlayerData ownData = PlayerData.get(this.player);
				PlayerData foreignData = PlayerData.get(Cosmetica.farPickPlayer);

				// if not loading
				if (ownData != PlayerData.TEMPORARY && foreignData != PlayerData.TEMPORARY) {
					Authentication.openSnipeScreen(null, foreignData, ownData);
					return;
				}
			}
			this.setScreen(new LoadingScreen(null, Minecraft.getInstance().options, 2));
		}
	}
}
