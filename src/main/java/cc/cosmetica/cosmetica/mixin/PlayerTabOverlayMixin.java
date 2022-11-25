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
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
	// ========================== //
	//         Shift Name         //
	// ========================== //

	// Why am I doing a redirect here?!
	// 2 reasons:
	// - 1. Compatability. This is a very funny place to put a redirect that I suspect no other mod probably will put, and it gives me the info I need.
	// - 2. Portability. Redirects are more portable than Inject with Local Capture. Cosmetica aims to support a wide range of Minecraft versions at any given
	//      time, so local captures are out of the question.

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerInfo;getProfile()Lcom/mojang/authlib/GameProfile;"), method = "render")
	private GameProfile capturePlayer(PlayerInfo instance) {
		GameProfile result = instance.getProfile();
		this.cosmetica_capturedPlayer = result;
		return result;
	}

	// ensure no one else takes this name, somehow
	@Unique
	private GameProfile cosmetica_capturedPlayer;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/network/chat/Component;FFI)I"), method = "render")
	public void beforeRenderName(PoseStack stack, int i, Scoreboard scoreboard, Objective objective, CallbackInfo info) {
		stack.pushPose();
		Cosmetica.prepareTabIcon(stack, this.cosmetica_capturedPlayer.getId(), this.cosmetica_capturedPlayer.getName());
	}

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/network/chat/Component;FFI)I"), method = "render")
	public void afterRenderName(PoseStack stack, int i, Scoreboard scoreboard, Objective objective, CallbackInfo info) {
		stack.popPose();
	}

	// ========================== //
	//         Render Icon        //
	// ========================== //

	@Inject(at = @At("HEAD"), method = "renderPingIcon")
	private void onRenderPingIcon(PoseStack stack, int p, int x, int y, PlayerInfo playerInfo, CallbackInfo ci) {
		Cosmetica.renderTabIcon(stack, x, y, playerInfo.getProfile().getId(), playerInfo.getProfile().getName());
	}
}
