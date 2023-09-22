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

package cc.cosmetica.cosmetica.mixin.textures;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.CosmeticaSkinManager;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player {
	public AbstractClientPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
		super(level, blockPos, f, gameProfile);
	}

	@Inject(at = @At("HEAD"), method = "isCapeLoaded", cancellable = true)
	private void isCosmeticaCapeLoaded(CallbackInfoReturnable<Boolean> info) {
		if (!Cosmetica.isProbablyNPC(this.uuid)) info.setReturnValue(PlayerData.has(this.uuid));
	}

	@Inject(at = @At("HEAD"), method = "getCloakTextureLocation", cancellable = true)
	private void addCosmeticaCapes(CallbackInfoReturnable<ResourceLocation> info) {
		if (!Cosmetica.isProbablyNPC(this.uuid)) { // ignore npcs
			ResourceLocation location = PlayerData.has(this.uuid) ? PlayerData.get(this).cape().getImage() : null; // get the location if cached
			if (location != null && !CosmeticaSkinManager.isUploaded(location)) location = null; // only actually get it if it's been uploaded
			info.setReturnValue(location); // set the return value to our one
		}
	}
}
