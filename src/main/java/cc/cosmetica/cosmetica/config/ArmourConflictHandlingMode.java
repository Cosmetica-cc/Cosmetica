package cc.cosmetica.cosmetica.config;

/**
 * The set of modes for handling server-declared conflicts between cosmetics and armour. This includes cape/elytra.
 *
 * @apiNote This will almost certainly be replaced by a better solution we have in the works for Cosmetica 2.0.
 * In the meantime, this is easier to implement and should satisfy everyone.
 */
public enum ArmourConflictHandlingMode {
	/**
	 * The default solution, and the solution used prior to this version.
	 */
	HIDE_COSMETICS("cosmetica.armourMode.hideCosmetics"),
	/**
	 * Hide the armour instead of the cosmetics.
	 */
	HIDE_ARMOUR("cosmetica.armourMode.hideArmour"),
	/**
	 * Show both the cosmetic and armour, as if the flag invoking this behaviour were never set.
	 */
	SHOW_BOTH("cosmetica.armourMode.showBoth");

	ArmourConflictHandlingMode(String languageKey) {
		this.languageKey = languageKey;
	}

	private final String languageKey;

	public String getLanguageKey() {
		return this.languageKey;
	}
}
