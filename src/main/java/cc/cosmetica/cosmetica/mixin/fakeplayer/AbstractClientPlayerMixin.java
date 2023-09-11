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
import cc.cosmetica.cosmetica.screens.fakeplayer.Playerish;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player implements Playerish {
	public AbstractClientPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile, ProfilePublicKey ppk) {
		super(level, blockPos, f, gameProfile, ppk);
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
		return PlayerData.get(this);
	}

	@Override
	public Vec3 getVelocity() {
		return this.getDeltaMovement();
	}
}
