package com.eyezah.cosmetics;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

public final class CosmeticsAPI {
	private CosmeticsAPI() {
		// NO-OP. Access Modifier changed to PRIVATE.
	}

	private static boolean playerLore = true;

	public static void displayPlayerLore(boolean enabled) {
		playerLore = enabled;
	}

	public static boolean isPlayerLoreEnabled() {
		return playerLore;
	}

	public static String lookUp(UUID uuid, String username, Set<UUID> lookingUp, Map<UUID, String> cache, BiFunction<UUID, String, String> lookupFunction) {
		synchronized (cache) {
			return cache.computeIfAbsent(uuid, uid -> {
				if (!lookingUp.contains(uuid)) { // if not already looking up, mark as looking up.
					lookingUp.add(uuid);

					Cosmetics.runOffthread(() -> {
						String associatedText = lookupFunction.apply(uuid, username);

						synchronized (cache) { // update the information with what we have gotten.
							cache.put(uuid, associatedText);
							lookingUp.remove(uuid);
						}
					});
				}

				return ""; // temporary name: blank.
			});
		}
	}
}
