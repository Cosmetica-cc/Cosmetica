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
import cc.cosmetica.cosmetica.ThreadPool;
import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import cc.cosmetica.cosmetica.utils.DebugMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Data class for Player Data
 *
 * @implNote Why isn't this a record? Well, it was a record. However, this mod is maintained across many versions, some of which use
 * older versions of java without records. Therefore, as this class is edited relatively often, it has been refactored into
 * a class to prevent merge conflicts every time it is edited.
 */
public final class PlayerData {
	private final String lore;
	private final boolean upsideDown;
	private final @Nullable ResourceLocation icon;
	private final boolean online;

	private final String prefix;
	private final String suffix;
	private final List<BakableModel> hats;
	private final CapeData cape;
	private final @Nullable BakableModel leftShoulderBuddy;
	private final @Nullable BakableModel rightShoulderBuddy;
	private final @Nullable BakableModel backBling;
	private final ResourceLocation skin;
	private final boolean slim;

	public PlayerData(String lore, boolean upsideDown, @Nullable ResourceLocation icon, boolean online, String prefix, String suffix, List<BakableModel> hats,
					  CapeData cape, @Nullable BakableModel leftShoulderBuddy, @Nullable BakableModel rightShoulderBuddy, @Nullable BakableModel backBling, ResourceLocation skin, boolean slim) {
		this.lore = lore;
		this.upsideDown = upsideDown;
		this.prefix = prefix;
		this.suffix = suffix;
		this.hats = hats;
		this.cape = cape;
		this.leftShoulderBuddy = leftShoulderBuddy;
		this.rightShoulderBuddy = rightShoulderBuddy;
		this.backBling = backBling;
		this.skin = skin;
		this.slim = slim;
		// 1.2.2
		this.icon = icon;
		this.online = online;
	}

	public String lore() {
		return lore;
	}

	public boolean upsideDown() {
		return upsideDown;
	}

	public String prefix() {
		return prefix;
	}

	public String suffix() {
		return suffix;
	}

	public List<BakableModel> hats() {
		return hats;
	}

	public BakableModel leftShoulderBuddy() {
		return leftShoulderBuddy;
	}

	public BakableModel rightShoulderBuddy() {
		return rightShoulderBuddy;
	}

	public BakableModel backBling() {
		return backBling;
	}

	public CapeData cape() {
		return this.cape;
	}

	public ResourceLocation skin() {
		return skin;
	}

	public boolean slim() {
		return slim;
	}

	// 1.2.2

	@Nullable
	public ResourceLocation icon() {
		return icon;
	}

	public boolean online() {
		return online;
	}

	// --

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		PlayerData that = (PlayerData) obj;
		return Objects.equals(this.lore, that.lore) &&
				this.upsideDown == that.upsideDown &&
				Objects.equals(this.icon, that.icon) &&
				this.online == that.online &&
				Objects.equals(this.prefix, that.prefix) &&
				Objects.equals(this.suffix, that.suffix) &&
				Objects.equals(this.cape, that.cape) &&
				Objects.equals(this.hats, that.hats) &&
				Objects.equals(this.leftShoulderBuddy, that.leftShoulderBuddy) &&
				Objects.equals(this.rightShoulderBuddy, that.rightShoulderBuddy) &&
				Objects.equals(this.backBling, that.backBling) &&
				Objects.equals(this.skin, that.skin) &&
				this.slim == that.slim;
	}

	@Override
	public int hashCode() {
		return Objects.hash(lore, upsideDown, icon, online, prefix, suffix, cape, hats, leftShoulderBuddy, rightShoulderBuddy, backBling, skin, slim);
	}

	@Override
	public String toString() {
		return "PlayerData[" +
				"lore=" + lore + ", " +
				"upsideDown=" + upsideDown + ", " +
				"icon=" + icon + ", " +
				"online=" + online + ", " +
				"prefix=" + prefix + ", " +
				"suffix=" + suffix + ", " +
				"cape=" + cape + ", " +
				"hats=" + hats + ", " +
				"leftShoulderBuddy=" + leftShoulderBuddy + ", " +
				"rightShoulderBuddy=" + rightShoulderBuddy + ", " +
				"backBling=" + backBling + ", " +
				"skin=" + skin + ", " +
				"slim=" + slim + ']';
	}

	public static PlayerData NONE = new PlayerData("", false, null, false, "", "", new ArrayList<>(), CapeData.NO_CAPE, null, null, null, DefaultPlayerSkin.getDefaultTexture(), false);
	public static PlayerData TEMPORARY = new PlayerData("", false, null, false, "", "", new ArrayList<>(), CapeData.NO_CAPE, null, null, null, DefaultPlayerSkin.getDefaultTexture(), false);

	private static Map<UUID, PlayerData> playerDataCache = new HashMap<>();
	private static Set<UUID> lookingUp = new HashSet<>();

	public static PlayerData get(Player player) {
		return get(player.getUUID(), player.getName().getString(), false);
	}

	public static PlayerData get(UUID uuid, String username, boolean sync) {
		if (Cosmetica.isProbablyNPC(uuid)) return PlayerData.NONE;

		Level level = Minecraft.getInstance().level;

		// if existing data exists

		synchronized (playerDataCache) {
			PlayerData existing = playerDataCache.get(uuid);

			if (existing != null) {
				// synchronised requests do not want temporary data returned!
				if (!(sync && existing == PlayerData.TEMPORARY)) {
					return existing;
				}
			} else {
				// start a new lookup
				lookingUp.add(uuid);
				playerDataCache.put(uuid, PlayerData.TEMPORARY);
			}
		}

		if (sync) {
			return lookupPlayerData(uuid, username, level);
		} else {
			Cosmetica.runOffthread(() -> {
				if (Cosmetica.api == null || Minecraft.getInstance().level != level) { // don't make the request if the level changed (in case the players are different between levels)!
					synchronized (playerDataCache) { // make sure temp values are removed
						playerDataCache.remove(uuid);
						lookingUp.remove(uuid);
					}
				}

				lookupPlayerData(uuid, username, level);
			}, ThreadPool.GENERAL_THREADS);

			return PlayerData.NONE;
		}
	}

	private static PlayerData lookupPlayerData(UUID uuid, String username, Level level) {
		DebugMode.log("Looking up player info for " + uuid + " (" + username + ")");
		AtomicReference<PlayerData> newDataHolder = new AtomicReference<>(PlayerData.NONE);

		Cosmetica.api.getUserInfo(uuid, username).ifSuccessfulOrElse(info -> {
			PlayerData newData = Cosmetica.newPlayerData(info, uuid);

			synchronized (playerDataCache) { // update the information with what we have gotten.
				playerDataCache.put(uuid, newData);
				lookingUp.remove(uuid);
			}

			newDataHolder.set(newData);
		}, Cosmetica.logErr("Error getting user info for " + uuid + " / " + username).andThen(re -> {
			synchronized (playerDataCache) {
				// check no other thread has gotten there first.
				// This could still be mistriggered if, say, level changes, player data is cleared, and a new request is made
				// So we check level too.
				if (Minecraft.getInstance().level == level && playerDataCache.get(uuid) == PlayerData.TEMPORARY) {
					lookingUp.remove(uuid);
				}
			}
		}));

		return newDataHolder.get();
	}

	/**
	 * Check whether the given player currently has data stored. It does not check whether that data is temporary.
	 * @param uuid the uuid of the player to check.
	 * @return whether the player currently has data stored.
	 */
	public static boolean has(UUID uuid) {
		synchronized (playerDataCache) {
			return playerDataCache.containsKey(uuid);
		}
	}

	public static PlayerData getCached(UUID player) {
		synchronized (playerDataCache) {
			return playerDataCache.get(player);
		}
	}

	public static void clear(UUID uuid) {
		synchronized (playerDataCache) {
			playerDataCache.remove(uuid);
		}
	}

	public static int getCacheSize() {
		synchronized (playerDataCache) {
			return playerDataCache.size();
		}
	}

	public static Collection<UUID> getCachedPlayers() {
		synchronized (playerDataCache) {
			return playerDataCache.keySet();
		}
	}

	public static void clearCaches() {
		playerDataCache = new HashMap<>();
	}
}
