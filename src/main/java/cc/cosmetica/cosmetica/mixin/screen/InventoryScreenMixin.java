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

package cc.cosmetica.cosmetica.mixin.screen;

import cc.cosmetica.cosmetica.Cosmetica;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
	@Unique
	private static boolean cosmetica_showNametagInThirdPerson;

	@Inject(at = @At("HEAD"), method = "renderEntityInInventory")
	private static void disableOwnNametagTemporarilyIfShown(GuiGraphics poseStack, int i, int j, int k, Quaternionf quaternionf, Quaternionf quaternionf2, LivingEntity livingEntity, CallbackInfo ci) {
		cosmetica_showNametagInThirdPerson = Cosmetica.getConfig().shouldShowNametagInThirdPerson();
		Cosmetica.getConfig().setShowNametagInThirdPerson(false);
	}

	@Inject(at = @At("RETURN"), method = "renderEntityInInventory")
	private static void reenableNametag(GuiGraphics poseStack, int i, int j, int k, Quaternionf quaternionf, Quaternionf quaternionf2, LivingEntity livingEntity, CallbackInfo ci) {
		Cosmetica.getConfig().setShowNametagInThirdPerson(cosmetica_showNametagInThirdPerson);
	}
}
