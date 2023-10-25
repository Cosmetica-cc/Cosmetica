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

package cc.cosmetica.cosmetica.mixin.textures;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.CosmeticaSkinManager;
import cc.cosmetica.cosmetica.cosmetics.CapeData;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.WeakReference;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player {
	public AbstractClientPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
		super(level, blockPos, f, gameProfile);
	}

	@Shadow protected abstract @Nullable PlayerInfo getPlayerInfo();

	// avoid allocating memory every time getSkin is called!
	@Unique private WeakReference<@Nullable PlayerSkin> cosmetica_cachedOldPlayerSkin = new WeakReference<>(null);

	@Inject(at = @At("HEAD"), method = "getSkin", cancellable = true)
	private void addCosmeticaCapes(CallbackInfoReturnable<PlayerSkin> info) {
		if (info.getReturnValue() != null) {
			GameProfile profile = this.getPlayerInfo().getProfile();

			if (!Cosmetica.isProbablyNPC(profile.getId())) { // ignore npcs
				if (!PlayerData.has(profile.getId()))
					return;

				CapeData cape = PlayerData.get(this).cape();
				ResourceLocation location = cape.getImage(); // get the location if cached
				if (location != null && !CosmeticaSkinManager.isUploaded(location))
					location = null; // only actually get it if it's been uploaded

				if (location != null) {
					@Nullable PlayerSkin cached = this.cosmetica_cachedOldPlayerSkin.get();

					if (cached != info.getReturnValue()) {
						this.cosmetica_cachedOldPlayerSkin = new WeakReference<>(cached);
						cape.clearSkinCache();
					}

					info.setReturnValue(cape.getSkin(info.getReturnValue()));
				}
			}
		}
	}
}
