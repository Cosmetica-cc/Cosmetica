package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Cosmetica;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public MixinPlayerRenderer(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> entityModel, float f) {
		super(context, entityModel, f);
	}

	@Inject(at = @At("HEAD"), method = "renderNameTag")
	protected void onRenderNameTag(Entity entity, Component component, PoseStack stack, MultiBufferSource multiBufferSource, int i, CallbackInfo info) {
		stack.pushPose();
		Cosmetica.onRenderNameTag(this.entityRenderDispatcher, entity, stack, multiBufferSource, this.getFont(), i);
	}

	@Inject(at = @At("RETURN"), method = "renderNameTag")
	protected void afterRenderNameTag(Entity entity, Component component, PoseStack stack, MultiBufferSource multiBufferSource, int i, CallbackInfo info) {
		stack.popPose();
	}
}
