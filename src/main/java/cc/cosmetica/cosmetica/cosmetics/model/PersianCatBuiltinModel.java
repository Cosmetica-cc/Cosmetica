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

package cc.cosmetica.cosmetica.cosmetics.model;

import cc.cosmetica.cosmetica.screens.fakeplayer.Playerish;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PersianCatBuiltinModel implements BuiltInModel {
	@Override
	public void render(PoseStack stack, MultiBufferSource multiBufferSource, EntityModelSet modelSet, Playerish player, boolean left, int packedLight) {
		LiveCatModel model = new LiveCatModel(modelSet.bakeLayer(ModelLayers.CAT));
		stack.pushPose();
		stack.translate(left ? 0.405 : -0.405, (player.isSneaking() ? -1.3 : -1.515D) + 1.07D, 0.0D);
		stack.scale(0.35f, 0.35f, 0.35f);

		model.lieDown(1.0f, 0.5f);

		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/cat/persian.png")));
		model.root.render(stack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
		stack.popPose();
	}

	@Override
	public Component notice() {
		return TextComponents.translatable("cosmetica.rsenotice.iranian");
	}
}
