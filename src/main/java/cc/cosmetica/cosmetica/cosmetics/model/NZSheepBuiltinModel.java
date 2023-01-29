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

import cc.cosmetica.cosmetica.cosmetics.Playerish;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

public class NZSheepBuiltinModel implements BuiltInModel {
	public NZSheepBuiltinModel(EntityModelSet modelSet) {
		this.fur = new LiveSheepModel(modelSet.bakeLayer(ModelLayers.SHEEP_FUR));
		this.body = new LiveSheepModel(modelSet.bakeLayer(ModelLayers.SHEEP));
	}

	private final LiveSheepModel fur;
	private final LiveSheepModel body;

	@Override
	public void render(PoseStack stack, MultiBufferSource multiBufferSource, Playerish player, boolean left, int packedLightProbably) {
		stack.pushPose();
		stack.scale(0.8f, 0.8f, 0.8f);
		stack.translate(left ? 0.42 : -0.42, (player.isSneaking() ? -1.3 : -1.6D) + 1.07D, 0.0D);
		stack.scale(0.35f, 0.35f, 0.35f);

		// Fur

		// calculate colour like a jeb sheep
		final int rate = 25;

		int tick = player.getLifetime() / rate + player.getPseudoId();
		int numColours = DyeColor.values().length;

		int prevTick = tick % numColours;
		int nextTick = (tick + 1) % numColours;
		float progress = ((float)(player.getLifetime() % rate) + 0) / (float) rate;

		float[] prevColours = Sheep.getColorArray(DyeColor.byId(prevTick));
		float[] nextColours = Sheep.getColorArray(DyeColor.byId(nextTick));

		float red = prevColours[0] * (1.0F - progress) + nextColours[0] * progress;
		float green = prevColours[1] * (1.0F - progress) + nextColours[1] * progress;
		float blue = prevColours[2] * (1.0F - progress) + nextColours[2] * progress;

		// render
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(fur.renderType(new ResourceLocation("textures/entity/sheep/sheep_fur.png")));
		fur.root.render(stack, vertexConsumer, packedLightProbably, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0f);

		stack.popPose();

		// Body

		stack.pushPose();

		stack.scale(0.8f, 0.8f, 0.8f);
		stack.translate(left ? 0.42 : -0.42, (player.isSneaking() ? -1.3 : -1.6D) + 1.07D, 0.0D);

		vertexConsumer = multiBufferSource.getBuffer(body.renderType(new ResourceLocation("textures/entity/sheep/sheep.png")));
		body.renderOnShoulder(stack, vertexConsumer, packedLightProbably, OverlayTexture.NO_OVERLAY);

		stack.popPose();
	}
}
