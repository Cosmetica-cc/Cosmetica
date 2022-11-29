/*
 * Copyright 2022 EyezahMC
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

import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data class for Player Data
 */
public final class PlayerData {
	private final String lore;
	private final boolean upsideDown;
	private final @Nullable ResourceLocation icon;
	private final boolean online;

	private final String prefix;
	private final String suffix;
	private final List<BakableModel> hats;
	private final @Nullable BakableModel leftShoulderBuddy;
	private final @Nullable BakableModel rightShoulderBuddy;
	private final @Nullable BakableModel backBling;
	private final String capeName;
	private final String capeId;
	private final boolean thirdPartyCape;
	private final @Nullable ResourceLocation cape;
	private final ResourceLocation skin;
	private final boolean slim;

	public PlayerData(String lore, boolean upsideDown, @Nullable ResourceLocation icon, boolean online, String prefix, String suffix, List<BakableModel> hats,
					  @Nullable BakableModel leftShoulderBuddy, @Nullable BakableModel rightShoulderBuddy, @Nullable BakableModel backBling, String capeName,
					  String capeId, boolean thirdPartyCape, @Nullable ResourceLocation cape, ResourceLocation skin, boolean slim) {
		this.lore = lore;
		this.upsideDown = upsideDown;
		this.prefix = prefix;
		this.suffix = suffix;
		this.hats = hats;
		this.leftShoulderBuddy = leftShoulderBuddy;
		this.rightShoulderBuddy = rightShoulderBuddy;
		this.backBling = backBling;
		this.capeName = capeName;
		this.capeId = capeId;
		this.thirdPartyCape = thirdPartyCape;
		this.cape = cape;
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

	public String capeName() {
		return capeName;
	}

	public String capeId() {
		return capeId;
	}

	public ResourceLocation cape() {
		return CustomLayer.CAPE_OVERRIDER.get(() -> this.cape);
	}

	public ResourceLocation legitCape() {
		return this.cape;
	}

	public boolean thirdPartyCape() {
		return thirdPartyCape;
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
				Objects.equals(this.hats, that.hats) &&
				Objects.equals(this.leftShoulderBuddy, that.leftShoulderBuddy) &&
				Objects.equals(this.rightShoulderBuddy, that.rightShoulderBuddy) &&
				Objects.equals(this.backBling, that.backBling) &&
				Objects.equals(this.capeName, that.capeName) &&
				Objects.equals(this.capeId, that.capeId) &&
				this.thirdPartyCape == that.thirdPartyCape &&
				Objects.equals(this.cape, that.cape) &&
				Objects.equals(this.skin, that.skin) &&
				this.slim == that.slim;
	}

	@Override
	public int hashCode() {
		return Objects.hash(lore, upsideDown, icon, online, prefix, suffix, hats, leftShoulderBuddy, rightShoulderBuddy, backBling, capeName, capeId, thirdPartyCape, cape, skin, slim);
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
				"hats=" + hats + ", " +
				"leftShoulderBuddy=" + leftShoulderBuddy + ", " +
				"rightShoulderBuddy=" + rightShoulderBuddy + ", " +
				"backBling=" + backBling + ", " +
				"capeName=" + capeName + ", " +
				"capeId=" + capeId + ", " +
				"thirdPartyCape=" + thirdPartyCape + ", " +
				"cape=" + cape + ", " +
				"skin=" + skin + ", " +
				"slim=" + slim + ']';
	}

	public static PlayerData NONE = new PlayerData("", false, null, false, "", "", new ArrayList<>(), null, null, null, "", "none", false, null, DefaultPlayerSkin.getDefaultSkin(), false);
	public static PlayerData TEMPORARY = new PlayerData("", false, null, false, "", "", new ArrayList<>(), null, null, null, "", "none", false, null, DefaultPlayerSkin.getDefaultSkin(), false);
}
