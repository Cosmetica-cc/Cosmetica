package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.cosmetics.BackBling;
import com.eyezah.cosmetics.cosmetics.Hat;
import com.eyezah.cosmetics.cosmetics.ShoulderBuddy;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public MixinPlayerEntityRenderer(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> entityModel, float f) {
		super(context, entityModel, f);
	}

	@Inject(at=@At("TAIL"), method="<init>", allow=1)
	private void init(EntityRendererProvider.Context context, boolean bl, CallbackInfo ci) {
		this.addLayer(new Hat<>(this));
		this.addLayer(new ShoulderBuddy<>(this, context.getModelSet()));
		this.addLayer(new BackBling<>(this, context.getModelSet()));
	}
}