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

	public static PlayerData NONE      = new PlayerData("", false, null, false, "", "", new ArrayList<>(), CapeData.NO_CAPE, null, null, null, DefaultPlayerSkin.getDefaultSkin(), false);
	public static PlayerData TEMPORARY = new PlayerData("", false, null, false, "", "", new ArrayList<>(), CapeData.NO_CAPE, null, null, null, DefaultPlayerSkin.getDefaultSkin(), false);
}
