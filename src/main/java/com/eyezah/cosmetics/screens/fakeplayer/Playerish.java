package com.eyezah.cosmetics.screens.fakeplayer;

/**
 * Something that's... playerish. Could be a player, could be a fake player.
 * Methods are amazingly named to avoid conflicts.
 */
public interface Playerish {
	/**
	 * Get the player's tick count, which is kinda like a lifetime I guess.
	 */
	int getLifetime();

	/**
	 * Get something that could be an entity id. Then again, maybe it isn't.
	 */
	int getPseudoId();

	/**
	 * Whether the player is sneaking.
	 */
	boolean isSneaking();
}
