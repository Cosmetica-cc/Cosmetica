package cc.cosmetica.cosmetica.mixin;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.utils.Debug;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SplashManager.class)
public class SplashManagerMixin {
	@Shadow
	@Final
	private List<String> splashes;

	@Inject(at = @At("RETURN"), method = "apply(Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
	private void afterApply(List<String> list, ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfo ci) {
		if (Debug.DEBUG_MODE) this.splashes.clear(); // in debug mode only have the custom one :)
		this.splashes.addAll(Cosmetica.getSplashes());
	}
}
