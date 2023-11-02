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

package cc.cosmetica.cosmetica.cosmetics;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.CosmeticaSkinManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.lang.ref.WeakReference;

public final class CosmeticaCapes {
	public CosmeticaCapes(Player player) {
		this.player = player;
	}

	private final Player player;

	// avoid allocating memory every time getSkin is called!
	@Unique
	private WeakReference<@Nullable PlayerSkin> cachedVanillaSkin = new WeakReference<>(null);

	@Nullable
	public PlayerSkin addCosmeticaCapes(GameProfile profile, PlayerSkin vanillaSkin) {
		if (!Cosmetica.isProbablyNPC(profile.getId())) { // ignore npcs
			if (!PlayerData.has(profile.getId()))
				return null;

			CapeData cape = PlayerData.get(player).cape();
			ResourceLocation location = cape.getImage(); // get the location if cached
			if (location != null && !CosmeticaSkinManager.isUploaded(location))
				location = null; // only actually get it if it's been uploaded

			if (location != null) {
				@Nullable PlayerSkin cached = cachedVanillaSkin.get();

				if (cached != vanillaSkin) {
					cachedVanillaSkin = new WeakReference<>(cached);
					cape.clearSkinCache();
				}

				return cape.getSkin(vanillaSkin);
			}
		}

		return null;
	}
}
