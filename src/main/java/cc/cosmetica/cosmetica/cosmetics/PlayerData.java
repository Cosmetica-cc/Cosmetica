package cc.cosmetica.cosmetica.cosmetics;

import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import cc.cosmetica.cosmetica.utils.Debug;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class for Player Data
 */
public record PlayerData(String lore, boolean upsideDown, String prefix, String suffix, List<BakableModel> hats, @Nullable BakableModel leftShoulderBuddy, @Nullable BakableModel rightShoulderBuddy, @Nullable BakableModel backBling, String capeName, String capeId, boolean thirdPartyCape, @Nullable ResourceLocation cape, ResourceLocation skin, boolean slim) {

	public static PlayerData NONE = new PlayerData("", false, "", "", new ArrayList<>(), null, null, null, "", "none", false, null, DefaultPlayerSkin.getDefaultSkin(), false);
	public static PlayerData TEMPORARY = new PlayerData("", false, "", "", new ArrayList<>(), null, null, null, "", "none", false, null, DefaultPlayerSkin.getDefaultSkin(), false);

	@Override
	public ResourceLocation cape() {
		return Debug.CAPE_OVERRIDER.get(() -> this.cape);
	}

	public ResourceLocation legitCape() {
		return this.cape;
	}
}
