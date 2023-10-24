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

import cc.cosmetica.api.Model;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.config.ArmourConflictHandlingMode;
import cc.cosmetica.cosmetica.cosmetics.BackBling;
import cc.cosmetica.cosmetica.cosmetics.Hats;
import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmourLayerMixin {
	/**
	 * Implements Armour Conflict Handling Mode: Hide Armour
	 */
	@Inject(at = @At("HEAD"), method = "renderArmorPiece", cancellable = true)
	private void onRenderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, LivingEntity livingEntity,
									EquipmentSlot equipmentSlot, int i, HumanoidModel humanoidModel, CallbackInfo info) {
		if (livingEntity instanceof AbstractClientPlayer) {
			// Filter out only armour
			// sike it's just non elytras and non empty to match behaviour elsewhere
			// this means we can easily treat mod jetpacks like armour (assuming they're not ArmorItem), but we also miss, say, wings->elytra
			// so perhaps we should just switch to use ArmorItem or make a tag
			// we also miss say, cosmetic flowers in hair with our broad hat strokes
			// todo this kind of needs to be revisited to see if we can find a better solution
			ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);

			if (itemStack.isEmpty() || itemStack.is(Items.ELYTRA)) {
				return;
			}

			// no pattern matching cause i don't want to rewrite this on 1.16.5
			AbstractClientPlayer player = (AbstractClientPlayer) livingEntity;
			if (player.isInvisible()) return;

			switch (equipmentSlot) {
			case CHEST:
				// don't run for config options that aren't Hide Armour
				if (Cosmetica.getConfig().getBackBlingConflictMode() != ArmourConflictHandlingMode.HIDE_ARMOUR) return;

				BakableModel backBling = BackBling.getBackBling(player);
				if (backBling == null) return;

				if (BackBling.chestplateConflict(player, backBling)) {
					info.cancel();
				}
				break;
			case HEAD:
				// don't run for config options that aren't Hide Armour
				if (Cosmetica.getConfig().getHatConflictMode() != ArmourConflictHandlingMode.HIDE_ARMOUR) return;

				List<BakableModel> hats = Hats.getHats(player);

				for (BakableModel model : hats) {
					// check if hat should hide with helmet
					if ((model.extraInfo() & Model.SHOW_HAT_WITH_HELMET) == 0) {
						// hat exists that conflicts with helmet. hide helmet as per config.
						info.cancel();
					}
				}
				break;
			}
		}
	}
}
