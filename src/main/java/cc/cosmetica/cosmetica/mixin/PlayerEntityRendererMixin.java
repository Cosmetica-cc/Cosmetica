package cc.cosmetica.cosmetica.mixin;

import cc.cosmetica.cosmetica.cosmetics.BackBling;
import cc.cosmetica.cosmetica.cosmetics.Hats;
import cc.cosmetica.cosmetica.cosmetics.ShoulderBuddies;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public PlayerEntityRendererMixin(EntityRenderDispatcher context, PlayerModel<AbstractClientPlayer> entityModel, float f) {
		super(context, entityModel, f);
	}

	@Inject(at=@At("TAIL"), method="<init>(Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;Z)V", allow=1)
	private void init(EntityRenderDispatcher context, boolean bl, CallbackInfo ci) {
		this.addLayer(new Hats<>(this));
		this.addLayer(new ShoulderBuddies<>(this));
		this.addLayer(new BackBling<>(this));
	}
}