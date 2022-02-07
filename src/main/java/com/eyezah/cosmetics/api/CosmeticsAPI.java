package com.eyezah.cosmetics.api;

import com.eyezah.cosmetics.Cosmetica;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * The API for Cosmetics Mod.
 */
public class CosmeticsAPI {
	/**
	 * Gets the player data for a player.
	 * @param player the player to get the data for.
	 * @return a blank default player data if it is still loading or failed to load. Otherwise, the data for this player.
	 */
	public static PlayerData getPlayerData(AbstractClientPlayer player) {
		return Cosmetica.getPlayerData(player);
	}
}
