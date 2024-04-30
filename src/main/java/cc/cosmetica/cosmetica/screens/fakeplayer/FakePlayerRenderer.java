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

package cc.cosmetica.cosmetica.screens.fakeplayer;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.mixin.fakeplayer.HumanoidModelAccessor;
import cc.cosmetica.cosmetica.mixin.fakeplayer.PlayerModelAccessor;
import cc.cosmetica.cosmetica.utils.LinearAlgebra;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class FakePlayerRenderer {
	public static Quaternionf cameraOrientation = LinearAlgebra.QUATERNION_ONE;

	// EntityRenderDispatcher#render
	public static void render(PoseStack stack, FakePlayer player, MultiBufferSource bufferSource, double xOffset, double yOffset, double zOffset, float rotation, float delta, int light) {
		try {
			Vec3 vec3 = getRenderOffset(player, delta);
			double x = xOffset + vec3.x();
			double y = yOffset + vec3.y();
			double z = zOffset + vec3.z();
			stack.pushPose();
			stack.translate(x, y, z);

			drawFakePlayer(player, rotation, delta, stack, bufferSource, light);

			stack.popPose();
		} catch (Throwable var24) {
			CrashReport crashReport = CrashReport.forThrowable(var24, "Rendering fake player in menu");
			crashReport.addCategory("Fake Player being rendered");

			CrashReportCategory crashReportCategory2 = crashReport.addCategory("Renderer details");
			crashReportCategory2.setDetail("Location", xOffset + "," + yOffset + "," + zOffset);
			crashReportCategory2.setDetail("Rotation", rotation);
			crashReportCategory2.setDetail("Delta", delta);
			throw new ReportedException(crashReport);
		}
	}

	private static Vec3 getRenderOffset(FakePlayer player, float delta) {
		return player.isSneaking() ? new Vec3(0.0D, -0.125D, 0.0D) : Vec3.ZERO;
	}

	// PlayerRenderer#render
	private static void drawFakePlayer(FakePlayer player, float rotation, float delta, PoseStack stack, MultiBufferSource bufferSource, int light) {
		setModelProperties(player);
		drawLivingEntity(player, rotation, delta, stack, bufferSource, light);
	}

	// PlayerRenderer#setModelProperties()
	private static void setModelProperties(FakePlayer fakePlayer) {
		PlayerModel playerModel = fakePlayer.getModel();

		playerModel.setAllVisible(true);
		playerModel.hat.visible = fakePlayer.isModelPartShown(PlayerModelPart.HAT);
		playerModel.jacket.visible = fakePlayer.isModelPartShown(PlayerModelPart.JACKET);
		playerModel.leftPants.visible = fakePlayer.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
		playerModel.rightPants.visible = fakePlayer.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
		playerModel.leftSleeve.visible = fakePlayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
		playerModel.rightSleeve.visible = fakePlayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
		playerModel.crouching = fakePlayer.isSneaking();

		if (fakePlayer.getMainArm() == HumanoidArm.RIGHT) {
			playerModel.rightArmPose = fakePlayer.isMainArmRaised() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
			playerModel.leftArmPose = HumanoidModel.ArmPose.EMPTY;
		} else {
			playerModel.rightArmPose = HumanoidModel.ArmPose.EMPTY;
			playerModel.leftArmPose = fakePlayer.isMainArmRaised() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
		}
	}

	// LivingEntityRenderer#render
	private static void drawLivingEntity(FakePlayer player, float rotation, float delta, PoseStack stack, MultiBufferSource bufferSource, int light) {
		stack.pushPose();
		var model = player.getModel();

		model.attackTime = 0;
		model.riding = false;
		model.young = false;

		float yRotBody = player.getYRotBody(delta);
		float yRotHead = player.getYRotHead(delta);
		float yRotDiff = yRotHead - yRotBody;
		float bob = delta;

		float xRot = player.getXRot(delta);

		if (player.getData().upsideDown()) {
			xRot *= -1.0F;
			yRotDiff *= -1.0F;
		}

		setupRotations(player, stack, bob, yRotBody, delta);

		stack.scale(-1.0F, -1.0F, 1.0F);
		stack.scale(0.9375F, 0.9375F, 0.9375F); // PlayerRenderer#scale
		stack.translate(0.0D, -1.5010000467300415D, 0.0D);

		float animationSpeed = 0.0f;//Mth.lerp(delta, player.animationSpeedOld, player.animationSpeed);
		float animationPosition = 0.0f;//player.animationPosition - player.animationSpeed * (1.0F - delta);

		if (animationSpeed > 1.0F) {
			animationSpeed = 1.0F;
		}

		//model.prepareMobModel(player, o, n, delta); only does swim stuff, not necessary
		modelSetupAnim(model, player, animationPosition, animationSpeed, bob, yRotDiff, xRot);

		RenderType renderType = getRenderType(player, true, false, false);

		if (renderType != null) {
			VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
			int packedOverlayCoords = getOverlayCoords(0.0f);
			model.renderToBuffer(stack, vertexConsumer, light, packedOverlayCoords, 1.0F, 1.0F, 1.0F, 1.0F);
		}

		// render layers

		for (MenuRenderLayer layer : player.getLayers()) {
			layer.render(stack, bufferSource, light, player, animationPosition, animationSpeed, delta, bob, yRotDiff, xRot);
		}

		stack.popPose();

		if (player.renderNametag) {
			renderNameTag(player, stack, bufferSource, light);
		}
	}

	private static RenderType getRenderType(FakePlayer player, boolean isVisible, boolean isInvisibleToPlayer, boolean isGlowing) {
		ResourceLocation resourceLocation = player.getSkin();

		if (isInvisibleToPlayer) {
			return RenderType.itemEntityTranslucentCull(resourceLocation);
		} else if (isVisible) {
			return player.getModel().renderType(resourceLocation);
		} else {
			return isGlowing ? RenderType.outline(resourceLocation) : null;
		}
	}

	private static void modelSetupAnim(PlayerModel<AbstractClientPlayer> model, FakePlayer player, float f, float g, float bob, float yRotDiff, float xRot) {
		model.head.yRot = yRotDiff * 0.017453292F;

		if (model.swimAmount > 0.0F) {
			model.head.xRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(model.swimAmount, model.head.xRot, xRot * 0.017453292F);
		} else {
			model.head.xRot = xRot * 0.017453292F;
		}

		model.body.yRot = 0.0F;
		model.rightArm.z = 0.0F;
		model.rightArm.x = -5.0F;
		model.leftArm.z = 0.0F;
		model.leftArm.x = 5.0F;
		float k = 1.0F;

		if (k < 1.0F) {
			k = 1.0F;
		}

		model.rightArm.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 2.0F * g * 0.5F / k;
		model.leftArm.xRot = Mth.cos(f * 0.6662F) * 2.0F * g * 0.5F / k;
		model.rightArm.zRot = 0.0F;
		model.leftArm.zRot = 0.0F;
		model.rightLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g / k;
		model.leftLeg.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 1.4F * g / k;
		model.rightLeg.yRot = 0.0F;
		model.leftLeg.yRot = 0.0F;
		model.rightLeg.zRot = 0.0F;
		model.leftLeg.zRot = 0.0F;
		ModelPart currentModel;

		if (model.riding) {
			currentModel = model.rightArm;
			currentModel.xRot += -0.62831855F;
			currentModel = model.leftArm;
			currentModel.xRot += -0.62831855F;
			model.rightLeg.xRot = -1.4137167F;
			model.rightLeg.yRot = 0.31415927F;
			model.rightLeg.zRot = 0.07853982F;
			model.leftLeg.xRot = -1.4137167F;
			model.leftLeg.yRot = -0.31415927F;
			model.leftLeg.zRot = -0.07853982F;
		}

		model.rightArm.yRot = 0.0F;
		model.leftArm.yRot = 0.0F;
		boolean bl3 = player.getMainArm() == HumanoidArm.RIGHT;
		boolean bl4;

		bl4 = bl3 ? model.leftArmPose.isTwoHanded() : model.rightArmPose.isTwoHanded();
		
		if (bl3 != bl4) {
			poseLeftArm(model);
			poseRightArm(model);
		} else {
			poseRightArm(model);
			poseLeftArm(model);
		}
		
		if (model.crouching) {
			model.body.xRot = 0.5F;
			currentModel = model.rightArm;
			currentModel.xRot += 0.4F;
			currentModel = model.leftArm;
			currentModel.xRot += 0.4F;
			model.rightLeg.z = 4.0F;
			model.leftLeg.z = 4.0F;
			model.rightLeg.y = 12.2F;
			model.leftLeg.y = 12.2F;
			model.head.y = 4.2F;
			model.body.y = 3.2F;
			model.leftArm.y = 5.2F;
			model.rightArm.y = 5.2F;
		} else {
			model.body.xRot = 0.0F;
			model.rightLeg.z = 0.1F;
			model.leftLeg.z = 0.1F;
			model.rightLeg.y = 12.0F;
			model.leftLeg.y = 12.0F;
			model.head.y = 0.0F;
			model.body.y = 0.0F;
			model.leftArm.y = 2.0F;
			model.rightArm.y = 2.0F;
		}

		if (model.rightArmPose != HumanoidModel.ArmPose.SPYGLASS) {
			AnimationUtils.bobModelPart(model.rightArm, bob, 1.0F);
		}

		if (model.leftArmPose != HumanoidModel.ArmPose.SPYGLASS) {
			AnimationUtils.bobModelPart(model.leftArm, bob, -1.0F);
		}

		if (model.swimAmount > 0.0F) {
			float l = f % 26.0F;
			HumanoidArm humanoidArm = player.getMainArm();
			float m = humanoidArm == HumanoidArm.RIGHT && model.attackTime > 0.0F ? 0.0F : model.swimAmount;
			float n = humanoidArm == HumanoidArm.LEFT && model.attackTime > 0.0F ? 0.0F : model.swimAmount;
			float o;

			if (l < 14.0F) {
				model.leftArm.xRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.xRot, 0.0F);
				model.rightArm.xRot = Mth.lerp(m, model.rightArm.xRot, 0.0F);
				model.leftArm.yRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.yRot, 3.1415927F);
				model.rightArm.yRot = Mth.lerp(m, model.rightArm.yRot, 3.1415927F);
				model.leftArm.zRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.zRot, 3.1415927F + 1.8707964F * ((HumanoidModelAccessor) model).invokeQuadraticArmUpdate(l) / ((HumanoidModelAccessor) model).invokeQuadraticArmUpdate(14.0F));
				model.rightArm.zRot = Mth.lerp(m, model.rightArm.zRot, 3.1415927F - 1.8707964F * ((HumanoidModelAccessor) model).invokeQuadraticArmUpdate(l) / ((HumanoidModelAccessor) model).invokeQuadraticArmUpdate(14.0F));
			} else if (l >= 14.0F && l < 22.0F) {
				o = (l - 14.0F) / 8.0F;
				model.leftArm.xRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.xRot, 1.5707964F * o);
				model.rightArm.xRot = Mth.lerp(m, model.rightArm.xRot, 1.5707964F * o);
				model.leftArm.yRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.yRot, 3.1415927F);
				model.rightArm.yRot = Mth.lerp(m, model.rightArm.yRot, 3.1415927F);
				model.leftArm.zRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.zRot, 5.012389F - 1.8707964F * o);
				model.rightArm.zRot = Mth.lerp(m, model.rightArm.zRot, 1.2707963F + 1.8707964F * o);
			} else if (l >= 22.0F && l < 26.0F) {
				o = (l - 22.0F) / 4.0F;
				model.leftArm.xRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.xRot, 1.5707964F - 1.5707964F * o);
				model.rightArm.xRot = Mth.lerp(m, model.rightArm.xRot, 1.5707964F - 1.5707964F * o);
				model.leftArm.yRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.yRot, 3.1415927F);
				model.rightArm.yRot = Mth.lerp(m, model.rightArm.yRot, 3.1415927F);
				model.leftArm.zRot = ((HumanoidModelAccessor) model).invokeRotlerpRad(n, model.leftArm.zRot, 3.1415927F);
				model.rightArm.zRot = Mth.lerp(m, model.rightArm.zRot, 3.1415927F);
			}

			model.leftLeg.xRot = Mth.lerp(model.swimAmount, model.leftLeg.xRot, 0.3F * Mth.cos(f * 0.33333334F + 3.1415927F));
			model.rightLeg.xRot = Mth.lerp(model.swimAmount, model.rightLeg.xRot, 0.3F * Mth.cos(f * 0.33333334F));
		}

		model.hat.copyFrom(model.head);

		model.leftPants.copyFrom(model.leftLeg);
		model.rightPants.copyFrom(model.rightLeg);
		model.leftSleeve.copyFrom(model.leftArm);
		model.rightSleeve.copyFrom(model.rightArm);
		model.jacket.copyFrom(model.body);

		ModelPart cloak = ((PlayerModelAccessor) model).getCloak();

		if (player.isSneaking()) {
			cloak.z = 1.4F;
			cloak.y = 1.85F;
		} else {
			cloak.z = 0.0F;
			cloak.y = 0.0F;
		}
	}
	
	private static void poseLeftArm(PlayerModel model) {
		switch(model.leftArmPose) {
		case EMPTY:
			model.leftArm.yRot = 0.0F;
			break;
		case BLOCK:
			model.leftArm.xRot = model.leftArm.xRot * 0.5F - 0.9424779F;
			model.leftArm.yRot = 0.5235988F;
			break;
		case ITEM:
			model.leftArm.xRot = model.leftArm.xRot * 0.5F - 0.31415927F;
			model.leftArm.yRot = 0.0F;
			break;
		}
	}

	private static void poseRightArm(PlayerModel model) {
		switch (model.rightArmPose) {
		case EMPTY:
			model.rightArm.yRot = 0.0F;
			break;
		case BLOCK:
			model.rightArm.xRot = model.rightArm.xRot * 0.5F - 0.9424779F;
			model.rightArm.yRot = -0.5235988F;
			break;
		case ITEM:
			model.rightArm.xRot = model.rightArm.xRot * 0.5F - 0.31415927F;
			model.rightArm.yRot = 0.0F;
			break;
		}
	}

	private static void setupRotations(FakePlayer player, PoseStack stack, float f, float g, float h) {
		stack.mulPose(LinearAlgebra.quaternionDegrees(LinearAlgebra.YP, 180.0F - g));

		if (player.getData().upsideDown()) {
			stack.translate(0.0D, EntityType.PLAYER.getDimensions().height() + 0.1, 0.0D);
			stack.mulPose(LinearAlgebra.quaternionDegrees(LinearAlgebra.ZP, 180.0F));
		}
	}

	private static int getOverlayCoords(float u) {
		return OverlayTexture.pack(OverlayTexture.u(u), OverlayTexture.v(false));
	}

	private static void renderNameTag(FakePlayer player, PoseStack stack, MultiBufferSource bufferSource, int light) {
		Component name = player.getDisplayName();

		boolean fullyRender = !player.renderDiscreteNametag();
		float yPosition = EntityType.PLAYER.getDimensions().height() + 0.5F;
		int offsetForDeadmau5 = "deadmau5".equals(name.getString()) ? -10 : 0;

		stack.pushPose();

		// add lore
		Cosmetica.renderLore(
				stack,
				cameraOrientation,
				Minecraft.getInstance().font,
				bufferSource,
				player.getData().lore(),
				player.getData().hats(),
				false,
				true,
				player.renderDiscreteNametag(),
				player.getData().upsideDown(),
				EntityType.PLAYER.getDimensions().height(),
				player.getModel().getHead().xRot,
				light);

		stack.translate(0.0D, yPosition, 0.0D);
		stack.mulPose(cameraOrientation);
		stack.scale(-0.025F, -0.025F, 0.025F);
		Matrix4f pose = stack.last().pose();
		float backgroundOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
		int k = (int)(backgroundOpacity * 255.0F) << 24;
		Font font = Minecraft.getInstance().font;
		float h = (float)(-font.width(name) / 2);
		font.drawInBatch(name, h, (float)offsetForDeadmau5, 0x20FFFFFF, false, pose, bufferSource, fullyRender ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, k, light);

		if (fullyRender) {
			font.drawInBatch(name, h, (float)offsetForDeadmau5, -1, false, pose, bufferSource, Font.DisplayMode.NORMAL, 0, light);
		}

		// add comsetica icon
		if (player.getData().icon() != null) {
			Cosmetica.renderIcon(stack, bufferSource, player, font, light, name);
		}

		stack.popPose();
	}
}
