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

package cc.cosmetica.cosmetica.mixin.screen;

import cc.cosmetica.cosmetica.utils.ExtendedTitleScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cc.cosmetica.cosmetica.Authentication.runAuthentication;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen implements ExtendedTitleScreen {
	protected TitleScreenMixin(Component component) {
		super(component);
	}

	@Inject(at = @At("HEAD"), method = "init")
	private void titleScreenInject(CallbackInfo ci) {
		runAuthentication(2);
	}

	/**
	 * Handles disabling the 'fade from white' mojang does on the titlescreen in 1.16.5. It looks fine in the main title screen animation, but is a bit of a
	 * 'stun grenade' when coming fromt he cosmetica tutorial.
	 */
	private boolean cosmetica_flashbang = true;

	@Override
	public void setFlashbang(boolean flashbang) {
		this.cosmetica_flashbang = false;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V", ordinal = 0))
	private void onWhiteFill(PoseStack stack, int x0, int y0, int x1, int y1, int colour) {
		if (this.cosmetica_flashbang) {
			fill(stack, x0, y0, x1, y1, colour);
		}
	}
}