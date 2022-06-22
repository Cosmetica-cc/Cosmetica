package com.eyezah.cosmetics.mixin.textures;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

/**
 * For memory management.
 */
@Mixin(TextureManager.class)
public interface TextureManagerAccessorMixin {
	@Accessor
	Map<ResourceLocation, AbstractTexture> getByPath();
	@Accessor
	Set<Tickable> getTickableTextures();
}
