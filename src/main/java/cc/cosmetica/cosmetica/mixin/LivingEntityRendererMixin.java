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
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
	@Inject(at = @At("HEAD"), method = "isEntityUpsideDown", cancellable = true)
	private static void alsoAustralians(LivingEntity entity, CallbackInfoReturnable<Boolean> info) {
		if (entity instanceof Player player && Cosmetica.shouldRenderUpsideDown(player)) {
			info.setReturnValue(true);
		}
	}

	@Inject(
			method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z",
			at = @At("HEAD"),
			cancellable = true
	)
	private void shouldShowName(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
		boolean thirdPerson = Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON;
		if (thirdPerson && Cosmetica.getConfig().shouldShowNametagInThirdPerson() && entity == Minecraft.getInstance().getCameraEntity()) cir.setReturnValue(Minecraft.renderNames());
	}
}
