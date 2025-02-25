/*
 * Copyright 2022, 2023 EyezahMC
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
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

	@ModifyReturnValue(at = @At("RETURN"), method = "getNameForDisplay")
	private Component modifyTablistDisplayName(Component original, @Local(argsOnly = true) PlayerInfo playerInfo) {
		PlayerData data = PlayerData.get(playerInfo.getProfile().getId(), playerInfo.getProfile().getName(), false);

		if (data.icon() != null) {
			return TextComponents.literal("\u2001").append(original);
		}
		return original;
	}

	// ========================== //
	//         Render Icon        //
	// ========================== //

	@Shadow @Final private Minecraft minecraft;

	@Inject(at = @At("HEAD"), method = "renderPingIcon")
	private void onRenderPingIcon(GuiGraphics stack, int p, int x, int y, PlayerInfo playerInfo, CallbackInfo ci) {
		boolean bl = this.minecraft.isLocalServer() || this.minecraft.getConnection().getConnection().isEncrypted();
		Cosmetica.renderTabIcon(stack.pose(), x + (bl ? 9 : 0), y, playerInfo.getProfile().getId(), playerInfo.getProfile().getName());
	}
}
