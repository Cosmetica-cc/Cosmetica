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

import cc.cosmetica.api.Model;
import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import cc.cosmetica.cosmetica.cosmetics.model.CosmeticStack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class Hats<T extends LivingEntity, P extends HumanoidModel<T>> extends CustomLayer<T, P> {

	public Hats(RenderLayerParent<T, P> renderLayerParent) {
		super(renderLayerParent);
	}

	@Override
	public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLight, Playerish player, float f, float g, float pitch, float bob, float yRotDiff, float xRot) {
		if (!player.isVisible()) return;
		List<BakableModel> hats = this.canOverridePlayerCosmetics(player) ? OVERRIDDEN.getList(() -> player.getCosmeticaPlayerData().hats()) : player.getCosmeticaPlayerData().hats();

		stack.pushPose();

		for (BakableModel modelData : hats) {
			if ((modelData.extraInfo() & Model.SHOW_HAT_WITH_HELMET) == 0 && player.isWearing(Playerish.Equipment.HELMET)) {
				continue; // disable hat flag
			}

			if ((modelData.extraInfo() & Model.LOCK_HAT_ORIENTATION) == 0) {
				doCoolRenderThings(modelData, this.getParentModel().getHead(), stack, multiBufferSource, packedLight, 0, 0.75f, 0);
			} else {
				doCoolRenderThings(modelData, this.getParentModel().body, stack, multiBufferSource, packedLight, 0, 0.77f, 0);
			}

			stack.scale(1.001f, 1.001f, 1.001f); // stop multiple hats conflicting
		}

		stack.popPose();
	}

	public static final CosmeticStack<BakableModel> OVERRIDDEN = new CosmeticStack();
}