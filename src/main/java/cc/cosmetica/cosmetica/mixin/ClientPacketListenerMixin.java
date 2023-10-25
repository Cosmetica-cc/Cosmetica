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

import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.utils.DebugMode;
import cc.cosmetica.cosmetica.Cosmetica;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
	@Shadow @Final private Minecraft minecraft;

	@Inject(at = @At("RETURN"), method = "handleLogin")
	private void onHandleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
		String address = "fake server " + System.currentTimeMillis();
		if (Minecraft.getInstance().getCurrentServer() != null && !Objects.equals(Minecraft.getInstance().getCurrentServer().ip, Cosmetica.authServer)) address = Minecraft.getInstance().getCurrentServer().ip;
		if (Cosmetica.currentServerAddressCache.isEmpty() || !Objects.equals(Cosmetica.currentServerAddressCache, address)) {
			Cosmetica.currentServerAddressCache = address;
			DebugMode.log("Clearing all player data due to login.");
			Cosmetica.clearAllCaches();
			PlayerData.get(this.minecraft.player);
		}
	}
}
