package cc.cosmetica.cosmetica.mixin.textures;

import cc.cosmetica.cosmetica.cosmetics.model.Models;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin {
	@Inject(at = @At("RETURN"), method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite$Info;IIIII)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;")
	private void onAtlasSprite(ResourceManager resourceManager, TextureAtlasSprite.Info tasinfo, int i, int j, int k, int l, int m, CallbackInfoReturnable<TextureAtlasSprite> info) {
		TextureAtlasSprite result = info.getReturnValue();

		if (result != null) {
			ResourceLocation location = tasinfo.name();

			if (location.getNamespace().equals("cosmetica") && location.getPath().matches("generated/reserved_[0-9]+")) {
				Models.TEXTURE_MANAGER.addAtlasSprite(result);
			}
		}
	}
}
