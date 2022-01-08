package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.utils.AuthenticatingScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import static com.eyezah.cosmetics.Cosmetics.connectScreen;

@Mixin(Deadmau5EarsLayer.class)
public abstract class MixinDeadmau5EarsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {


	public MixinDeadmau5EarsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent) {
		super(renderLayerParent);
	}

	@Inject(at = @At("HEAD"), method = "render")
	public void deadmau5Render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer, float f, float g, float h, float j, float k, float l) {
		System.out.println("rendering deadmau5 ears");
		if ("EYE2AH".equals(abstractClientPlayer.getName().getString()) && abstractClientPlayer.isSkinLoaded() && !abstractClientPlayer.isInvisible() || true) {
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(abstractClientPlayer.getSkinTextureLocation()));
			int m = LivingEntityRenderer.getOverlayCoords(abstractClientPlayer, 0.0F);

			for(int n = 0; n < 2; ++n) {
				float o = Mth.lerp(h, abstractClientPlayer.yRotO, abstractClientPlayer.getYRot()) - Mth.lerp(h, abstractClientPlayer.yBodyRotO, abstractClientPlayer.yBodyRot);
				float p = Mth.lerp(h, abstractClientPlayer.xRotO, abstractClientPlayer.getXRot());
				poseStack.pushPose();
				poseStack.mulPose(Vector3f.YP.rotationDegrees(o));
				poseStack.mulPose(Vector3f.XP.rotationDegrees(p));
				poseStack.translate((double)(0.375F * (float)(n * 2 - 1)), 0.0D, 0.0D);
				poseStack.translate(0.0D, -0.375D, 0.0D);
				poseStack.mulPose(Vector3f.XP.rotationDegrees(-p));
				poseStack.mulPose(Vector3f.YP.rotationDegrees(-o));
				float q = 1.3333334F;
				poseStack.scale(1.3333334F, 1.3333334F, 1.3333334F);
				((PlayerModel) this.getParentModel()).renderEars(poseStack, vertexConsumer, i, m);
				poseStack.popPose();
			}
		}
	}
}
