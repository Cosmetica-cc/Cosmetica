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
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
	// =============================================== //
	//         Add Full Space Character to Name        //
	// =============================================== //

	@Inject(at = @At("RETURN"), method = "getNameForDisplay", cancellable = true)
	private void modifyTablistDisplayName(PlayerInfo playerInfo, CallbackInfoReturnable<Component> info) {
		PlayerData data = Cosmetica.getPlayerData(playerInfo.getProfile().getId(), playerInfo.getProfile().getName(), false);

		if (data.icon() != null) {
			info.setReturnValue(TextComponents.literal("\u2001").append(info.getReturnValue()));
		}
	}

	// ========================== //
	//         Render Icon        //
	// ========================== //

	@Shadow @Final private Minecraft minecraft;

	@Inject(at = @At("HEAD"), method = "renderPingIcon")
	private void onRenderPingIcon(PoseStack stack, int p, int x, int y, PlayerInfo playerInfo, CallbackInfo ci) {
		boolean bl = this.minecraft.isLocalServer() || this.minecraft.getConnection().getConnection().isEncrypted();
		Cosmetica.renderTabIcon(stack, x + (bl ? 9 : 0), y, playerInfo.getProfile().getId(), playerInfo.getProfile().getName());
	}
}
