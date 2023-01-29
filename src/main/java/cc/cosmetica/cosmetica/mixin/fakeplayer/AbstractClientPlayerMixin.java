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

package cc.cosmetica.cosmetica.mixin.fakeplayer;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.cosmetics.Playerish;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player implements Playerish {
	@Shadow public abstract boolean isCapeLoaded();

	@Shadow public abstract @Nullable ResourceLocation getCloakTextureLocation();

	public AbstractClientPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
		super(level, blockPos, f, gameProfile);
	}

	@Override
	public int getLifetime() {
		return this.tickCount;
	}

	@Override
	public int getPseudoId() {
		return this.getId();
	}

	@Override
	public boolean isSneaking() {
		return this.isCrouching();
	}

	@Override
	public boolean renderDiscreteNametag() {
		return this.isDiscrete();
	}

	@Override
	public PlayerData getCosmeticaPlayerData() {
		return Cosmetica.getPlayerData(this);
	}

	@Override
	public Vec3 getVelocity() {
		return this.getDeltaMovement();
	}

	@Override
	public boolean isVisible() {
		return !this.isInvisible();
	}

	@Override
	public boolean isWearing(Equipment equipment) {
		switch (equipment) {
		case HELMET:
			return this.hasItemInSlot(EquipmentSlot.HEAD);
		case CHESTPLATE:
			return this.hasItemInSlot(EquipmentSlot.CHEST) && this.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA;
		case ELYTRA:
			return this.hasItemInSlot(EquipmentSlot.CHEST) && this.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
		case LEGGINGS:
			return this.hasItemInSlot(EquipmentSlot.LEGS);
		case BOOTS:
			return this.hasItemInSlot(EquipmentSlot.FEET);
		case LEFT_PARROT:
			return !this.getShoulderEntityLeft().isEmpty();
		case RIGHT_PARROT:
			return !this.getShoulderEntityRight().isEmpty();
		case CAPE:
			return this.isCapeLoaded() && this.isModelPartShown(PlayerModelPart.CAPE) && this.getCloakTextureLocation() != null;
		default:
			return false;
		}
	}
}
