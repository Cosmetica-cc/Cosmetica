package com.eyezah.cosmetics.mixin;

import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import com.eyezah.cosmetics.Cosmetica;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import static com.eyezah.cosmetics.Cosmetica.getPlayerData;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
	@Shadow
	@Final
	private EntityRenderDispatcher entityRenderDispatcher;

	@Shadow
	@Final
	private Font font;

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getDisplayName()Lnet/minecraft/network/chat/Component;"), method = "render")
	private Component getDisplayName(Entity entity) {
		String prefix = getPlayerData(entity.getUUID(), entity.getName().getString()).prefix();
		String suffix = getPlayerData(entity.getUUID(), entity.getName().getString()).suffix();

		return new TextComponent(prefix + entity.getDisplayName().getString() + suffix);
	}

	@Inject(at = @At("HEAD"), method = "renderNameTag")
	protected void onRenderNameTag(Entity entity, Component component, PoseStack stack, MultiBufferSource multiBufferSource, int i, CallbackInfo info) {
		stack.pushPose();
		Cosmetica.onRenderNameTag(this.entityRenderDispatcher, entity, stack, multiBufferSource, this.font, i);
	}

	@Inject(at = @At("RETURN"), method = "renderNameTag")
	protected void afterRenderNameTag(Entity entity, Component component, PoseStack stack, MultiBufferSource multiBufferSource, int i, CallbackInfo info) {
		stack.popPose();
	}
}
