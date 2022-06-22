package com.eyezah.cosmetics.mixin.textures;

import com.eyezah.cosmetics.utils.Scheduler;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public class TextureManagerMixin {
	@Inject(at = @At("RETURN"), method = "tick")
	private void onTick(CallbackInfo info) {
		Scheduler.executeScheduledTasks(Scheduler.Location.TEXTURE_TICK);
	}
}
