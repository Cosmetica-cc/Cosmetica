package cc.cosmetica.cosmetica.mixin;

import cc.cosmetica.cosmetica.cosmetics.model.Models;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
	@Inject(at = @At("RETURN"), method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/client/resources/model/ModelBakery;")
	private void captureBakery(ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<ModelBakery> info) {
		Models.thePieShopDownTheRoad = info.getReturnValue();
	}
}
