package com.eyezah.cosmetics.utils;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.mixin.textures.NativeImageAccessorMixin;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class NativeTexture extends AbstractTexture {
	public NativeTexture(ResourceLocation path, Supplier<NativeImage> image) {
		this.imageSupplier = image;
		this.path = path;

		Debug.info("Uploading native texture {}", this.path);

		this.image = this.imageSupplier.get();

		if (this.image == null) {
			Cosmetica.LOGGER.warn("Sorry texture machine broke for {}", this.path);
		}
	}

	private final ResourceLocation path;
	private final Supplier<NativeImage> imageSupplier;

	@Nullable
	private NativeImage image;

	@Override
	public void load(ResourceManager resourceManager) {
		if (this.image == null) {
			return;
		}

		if (((NativeImageAccessorMixin) (Object) this.image).getPixels() == 0) {
			if (RenderSystem.isOnRenderThreadOrInit()) {
				this.reload();
			} else {
				RenderSystem.recordRenderCall(this::reload);
			}

			return;
		}

		this.upload();
	}

	private void reload() {
		Debug.info("Re-uploading native texture {}", this.path);
		this.image = this.imageSupplier.get();

		if (this.image == null) {
			Cosmetica.LOGGER.warn("Sorry texture machine broke for {}", this.path);
		}
		else {
			this.upload();
		}
	}

	private void upload() {
		TextureUtil.prepareImage(this.getId(), 0, this.image.getWidth(), this.image.getHeight());
		this.image.upload(0, 0, 0, 0, 0, this.image.getWidth(), this.image.getHeight(), this.blur, false, false, true);
	}
}
