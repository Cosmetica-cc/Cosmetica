package com.eyezah.cosmetics.mixin;

import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import com.eyezah.cosmetics.Cosmetics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import static com.eyezah.cosmetics.Cosmetics.getPlayerData;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
	@Shadow
	@Final
	private EntityRenderDispatcher entityRenderDispatcher;

	@Shadow
	@Final
	private Font font;

	@Inject(at = @At("RETURN"), method = "renderNameTag", cancellable = true)
	protected void onRenderNameTag(Entity entity, Component component, PoseStack matrixStack, MultiBufferSource multiBufferSource, int i, CallbackInfo info) {
		Cosmetics.onRenderNameTag(this.entityRenderDispatcher, entity, matrixStack, multiBufferSource, this.font, i);
		if (entity instanceof RemotePlayer player) {
			String prefix = getPlayerData(entity.getUUID(), player.getName().getString()).prefix;
			String suffix = getPlayerData(entity.getUUID(), player.getName().getString()).suffix;
			if (!(prefix + suffix).equals("")) {
				double d = this.entityRenderDispatcher.distanceToSqr(entity);
				if (!(d > 4096.0D)) {
					boolean bl = !entity.isDiscrete();
					float f = entity.getBbHeight() + 0.5F;
					int j = "deadmau5".equals(component.getString()) ? -10 : 0;
					matrixStack.pushPose();
					matrixStack.translate(0.0D, (double)f, 0.0D);
					matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
					matrixStack.scale(-0.025F, -0.025F, 0.025F);
					Matrix4f matrix4f = matrixStack.last().pose();
					float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
					int k = (int)(g * 255.0F) << 24;
					component = new TextComponent(prefix + component.getString() + suffix);
					float h = (float)(-font.width((FormattedText)component) / 2);
					font.drawInBatch(component, h, (float)j, 553648127, false, matrix4f, multiBufferSource, bl, k, i);
					if (bl) {
						font.drawInBatch((Component)component, h, (float)j, -1, false, matrix4f, multiBufferSource, false, 0, i);
					}

					matrixStack.popPose();
				}
				info.cancel();
			}
		}
	}
}
