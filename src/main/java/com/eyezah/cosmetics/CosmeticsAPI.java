package com.eyezah.cosmetics;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

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

	public static String lookUp(UUID uuid, Set<UUID> lookingUp, Map<UUID, String> cache, Function<UUID, String> lookupFunction) {
		synchronized (cache) {
			return cache.computeIfAbsent(uuid, uid -> {
				if (!lookingUp.contains(uuid)) { // if not already looking up, mark as looking up.
					lookingUp.add(uuid);

					Cosmetics.runAsyncLookup(() -> {
						String associatedText = lookupFunction.apply(uuid);

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
