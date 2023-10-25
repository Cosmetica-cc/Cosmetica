/*
 * Copyright 2022, 2023 EyezahMC
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

package cc.cosmetica.cosmetica.mixin;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.config.ArmourConflictHandlingMode;
import cc.cosmetica.cosmetica.cosmetics.BackBling;
import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraLayer.class)
public class ElytraLayerMixin {
	@Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
			at = @At("HEAD"), cancellable = true)
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, LivingEntity livingEntity,
					   float f, float g, float h, float j, float k, float l, CallbackInfo info) {
		if (livingEntity instanceof AbstractClientPlayer) {
			// no pattern matching cause i don't want to rewrite this on 1.16.5
			AbstractClientPlayer player = (AbstractClientPlayer) livingEntity;
			if (player.isInvisible()) return;

			// don't run for config options that aren't Hide Armour
			if (Cosmetica.getConfig().getBackBlingElytraConflictMode() != ArmourConflictHandlingMode.HIDE_ARMOUR) return;

			BakableModel backBling = BackBling.getBackBling(player);
			if (backBling == null) return;

			if (BackBling.capeElytraConflict(player, backBling)) {
				info.cancel();
			}
		}
	}
}
