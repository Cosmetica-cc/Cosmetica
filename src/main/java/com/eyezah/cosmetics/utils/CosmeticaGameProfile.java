package com.eyezah.cosmetics.utils;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

/**
 * Marker for cosmetica game profiles.
 */
public class CosmeticaGameProfile extends GameProfile {
	public CosmeticaGameProfile(UUID id, String name) {
		super(id, name);
	}
}
