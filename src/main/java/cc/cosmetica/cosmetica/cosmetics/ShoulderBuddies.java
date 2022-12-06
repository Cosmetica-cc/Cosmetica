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
import cc.cosmetica.cosmetica.cosmetics.model.CosmeticStack;
import cc.cosmetica.cosmetica.screens.fakeplayer.Playerish;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.screens.fakeplayer.MenuRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

import java.util.OptionalInt;

public class ShoulderBuddies<T extends AbstractClientPlayer> extends CustomLayer<T, PlayerModel<T>> implements MenuRenderLayer {
	private ModelManager modelManager;
	private EntityModelSet entityModelSet;

	public ShoulderBuddies(RenderLayerParent<T, PlayerModel<T>> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.modelManager = Minecraft.getInstance().getModelManager();
		this.entityModelSet = entityModelSet;
	}

	@Override
	public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLight, T player, float f, float g, float pitch, float j, float k, float l) {
		if (player.isInvisible()) return;

		boolean canOverridePlayerCosmetics = this.canOverridePlayerCosmetics(player);

		PlayerData playerData = Cosmetica.getPlayerData(player);
		
		BakableModel left = canOverridePlayerCosmetics ? LEFT_OVERRIDDEN.get(playerData::leftShoulderBuddy) : playerData.leftShoulderBuddy();
		BakableModel right = canOverridePlayerCosmetics ? RIGHT_OVERRIDDEN.get(playerData::rightShoulderBuddy) : playerData.rightShoulderBuddy();

		if (left != null && ((left.extraInfo() & Model.SHOW_SHOULDER_BUDDY_WITH_PARROT) != 0 || player.getShoulderEntityLeft().isEmpty())) render(left, stack, multiBufferSource, packedLight, (Playerish) player, true);
		if (right != null && ((right.extraInfo() & Model.SHOW_SHOULDER_BUDDY_WITH_PARROT) != 0 || player.getShoulderEntityRight().isEmpty())) render(right, stack, multiBufferSource, packedLight, (Playerish) player, false);
	}

	@Override
	public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, FakePlayer player, float o, float n, float delta, float bob, float yRotDiff, float xRot) {
		BakableModel left = LEFT_OVERRIDDEN.get(() -> player.getData().leftShoulderBuddy());
		BakableModel right = RIGHT_OVERRIDDEN.get(() -> player.getData().rightShoulderBuddy());

		if (left != null) render(left, stack, bufferSource, packedLight, player, true);
		if (right != null) render(right, stack, bufferSource, packedLight, player, false);
	}

	public void render(BakableModel modelData, PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, Playerish player, boolean left) {
		stack.pushPose();

		if (modelData.id().equals("-sheep")) { // builtin live sheep
			LiveSheepModel model = new LiveSheepModel(entityModelSet.bakeLayer(ModelLayers.SHEEP_FUR));
			stack.pushPose();
			stack.scale(0.8f, 0.8f, 0.8f);
			stack.translate(left ? 0.42 : -0.42, (player.isSneaking() ? -1.3 : -1.6D) + 1.07D, 0.0D);
			stack.scale(0.35f, 0.35f, 0.35f);

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
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/sheep/sheep_fur.png")));
			model.root.render(stack, vertexConsumer, packedLightProbably, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0f);

			stack.popPose();

			model = new LiveSheepModel(entityModelSet.bakeLayer(ModelLayers.SHEEP));

			stack.pushPose();

			stack.scale(0.8f, 0.8f, 0.8f);
			stack.translate(left ? 0.42 : -0.42, (player.isSneaking() ? -1.3 : -1.6D) + 1.07D, 0.0D);

			vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/sheep/sheep.png")));
			model.renderOnShoulder(stack, vertexConsumer, packedLightProbably, OverlayTexture.NO_OVERLAY);

			stack.popPose();
		} else {
			boolean staticPosition = staticOverride.orElse(modelData.extraInfo() & Model.LOCK_SHOULDER_BUDDY_ORIENTATION) == Model.LOCK_SHOULDER_BUDDY_ORIENTATION;

			if (staticPosition) {
				stack.translate(left ? 0.375 : -0.375, -0.2, player.isSneaking() ? -0.16 : 0);
				doCoolRenderThings(modelData, this.getParentModel().body, stack, multiBufferSource, packedLightProbably, 0, 0.044f, 0, !left && (modelData.extraInfo() & Model.DONT_MIRROR_SHOULDER_BUDDY) == 0);
			} else {
				ModelPart modelPart = left ? this.getParentModel().leftArm : this.getParentModel().rightArm;
				doCoolRenderThings(modelData, modelPart, stack, multiBufferSource, packedLightProbably, 0, 0.37f, 0, !left && (modelData.extraInfo() & Model.DONT_MIRROR_SHOULDER_BUDDY) == 0);
			}
		}

		stack.popPose();
	}

	public static final CosmeticStack<BakableModel> LEFT_OVERRIDDEN = new CosmeticStack();
	public static final CosmeticStack<BakableModel> RIGHT_OVERRIDDEN = new CosmeticStack();
	public static OptionalInt staticOverride = OptionalInt.empty();
}
