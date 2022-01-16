package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.cosmetics.model.ModifiableAtlasSprite;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas {
	@Redirect(at = @At(value = "NEW", target = "net/minecraft/client/renderer/texture/TextureAtlasSprite"), method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite$Info;IIIII)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;")
	private TextureAtlasSprite createAtlasSprite(TextureAtlas textureAtlas, TextureAtlasSprite.Info info, int i, int j, int k, int l, int m, NativeImage nativeImage) {
		ResourceLocation location = info.name();

		if (location.getNamespace().equals("extravagant_cosmetics") && location.getPath().matches(".*reserved_[0-9]+.*")) { // not sure what the format is so here's the catch-all for all the possible formats I think it could be
			return new ModifiableAtlasSprite(textureAtlas, info, i, j, k, l, m, nativeImage);
		} else {
			return MixinTextureAtlasSpriteInvoker.create(textureAtlas, info, i, j, k, l, m, nativeImage);
		}
	}
}
