package cc.cosmetica.cosmetica.mixin.fakeplayer;

import cc.cosmetica.cosmetica.Cosmetica;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private Minecraft minecraft;

	@Inject(at = @At("RETURN"), method = "pick")
	private void onPick(float yawProbably, CallbackInfo info) {
		Cosmetica.cinder(this.minecraft, yawProbably);
	}
}
