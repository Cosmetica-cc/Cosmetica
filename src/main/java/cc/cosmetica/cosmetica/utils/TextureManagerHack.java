package cc.cosmetica.cosmetica.utils;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

public interface TextureManagerHack {
    /**
     * Get the texture at the given location or the default texture.
     * @param location the location.
     * @param or if no texture is present.
     * @return the texture at the given location if it exists else the texture provided in default.
     */
    AbstractTexture cosmetica$getTextureOr(ResourceLocation location, AbstractTexture or);
}
