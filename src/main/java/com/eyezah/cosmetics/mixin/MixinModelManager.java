package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.cosmetics.shoulderbuddies.model.Models;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelManager.class)
public class MixinModelManager {
	@Inject(at = @At("RETURN"), method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/client/resources/model/ModelBakery;")
	private void captureBakery(ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<ModelBakery> info) {
		Models.thePieShopDownTheRoad = info.getReturnValue();
	}

	@Inject(at = @At("RETURN"), method = "apply(Lnet/minecraft/client/resources/model/ModelBakery;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
	private void loadTest(ModelBakery modelBakery, ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfo ci) {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) Models.loadTestResource();
	}
}
