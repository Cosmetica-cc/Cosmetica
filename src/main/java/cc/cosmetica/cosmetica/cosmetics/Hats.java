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

package cc.cosmetica.cosmetica.cosmetics;

import cc.cosmetica.api.Model;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.config.ArmourConflictHandlingMode;
import cc.cosmetica.cosmetica.cosmetics.model.CosmeticStack;
import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.screens.fakeplayer.MenuRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class Hats<T extends Player> extends CustomLayer<T, PlayerModel<T>> implements MenuRenderLayer {

	public Hats(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	@Override
	public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLight, T player, float f, float g, float pitch, float j, float k, float l) {
		if (player.isInvisible()) return;
		List<BakableModel> hats = getHats(player);

		stack.pushPose();

		for (BakableModel modelData : hats) {
			if ((modelData.extraInfo() & Model.SHOW_HAT_WITH_HELMET) == 0 && player.hasItemInSlot(EquipmentSlot.HEAD)) {
				if (Cosmetica.getConfig().getHatConflictMode() == ArmourConflictHandlingMode.HIDE_COSMETICS) {
					continue; // disable hat flag
				}
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

	@Override
	public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, FakePlayer player, float o, float n, float delta, float bob, float yRotDiff, float xRot) {
		List<BakableModel> hats = OVERRIDDEN.getList(() -> player.getData().hats());

		stack.pushPose();

		for (BakableModel modelData : hats) {
			if ((modelData.extraInfo() & Model.LOCK_HAT_ORIENTATION) == 0) {
				doCoolRenderThings(modelData, this.getParentModel().getHead(), stack, bufferSource, packedLight, 0, 0.75f, 0);
			} else {
				doCoolRenderThings(modelData, this.getParentModel().body, stack, bufferSource, packedLight, 0, 0.77f, 0);
			}

			stack.scale(1.001f, 1.001f, 1.001f); // stop multiple hats conflicting
		}

		stack.popPose();
	}

	public static final CosmeticStack<BakableModel> OVERRIDDEN = new CosmeticStack();

	public static List<BakableModel> getHats(Player player) {
		return canOverridePlayerCosmetics(player) ?
				OVERRIDDEN.getList(() -> PlayerData.get(player).hats()) :
				PlayerData.get(player).hats();
	}
}