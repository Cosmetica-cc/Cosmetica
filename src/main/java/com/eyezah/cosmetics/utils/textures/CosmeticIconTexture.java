package com.eyezah.cosmetics.utils.textures;

import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.Model;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.mixin.textures.NativeImageAccessorMixin;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CosmeticIconTexture extends HttpTexture implements Tickable {
	public CosmeticIconTexture(@Nullable File file, String url, CosmeticType<?> type, int flags) {
		super(file, url, new ResourceLocation("cosmetica", "textures/gui/loading.png"), false, null);
		this.url = url;
		this.indicators = new HashSet<>();

		// CosmeticType is a class not an enum so no, I cannot use switch here, Mr. Picky.
		// Why is it a class? So I can throw generics in it for use in various api methods. I would have totally made it an enum if I didn't need to do that.
		if (type == CosmeticType.CAPE) {
			if (flags > 0) {
				this.indicators.add(InfoIcons.ANIMATED);
			}
		}
		else if (type == CosmeticType.BACK_BLING) {
			if ((flags & Model.SHOW_BACK_BLING_WITH_CAPE) > 0) {
				this.indicators.add(InfoIcons.SW_CAPE);
			}

			if ((flags & Model.SHOW_BACK_BLING_WITH_CHESTPLATE) > 0) {
				this.indicators.add(InfoIcons.SW_CHESTPLATE);
			}
		}
		else if (type == CosmeticType.SHOULDER_BUDDY) {
			if ((flags & Model.SHOW_SHOULDER_BUDDY_WITH_PARROT) > 0) {
				this.indicators.add(InfoIcons.SW_PARROT);
			}

			if ((flags & Model.LOCK_SHOULDER_BUDDY_ORIENTATION) > 0) {
				this.indicators.add(InfoIcons.LOCK_TO_TORSO);
			}

			if ((flags & Model.DONT_MIRROR_SHOULDER_BUDDY) == 0) {
				this.indicators.add(InfoIcons.MIRROR);
			}
		}
		else {
			if ((flags & Model.SHOW_HAT_WITH_HELMET) > 0) {
				this.indicators.add(InfoIcons.SW_HELMET);
			}

			if ((flags & Model.LOCK_HAT_ORIENTATION) > 0) {
				this.indicators.add(InfoIcons.LOCK_TO_TORSO);
			}
		}
	}

	private int frameHeight;
	private int frames;
	private int frame;
	private int tick;
	private NativeImage image;
	private final String url;
	private final Set<String> indicators;

	public void firstUpload(NativeImage image, boolean loading) {
		// memory management
		if (this.image != null && ((NativeImageAccessorMixin)(Object)this.image).getPixels() != 0L) {
			//Debug.info("Closing image on thread {} due to load. Are we allowed? {}", Thread.currentThread(), RenderSystem.isOnRenderThreadOrInit());
			this.image.close();
			//Debug.info("Closed image.");
		}

		this.image = image;

		this.frames = image.getHeight() / image.getWidth();
		this.frameHeight = image.getHeight() / this.frames;
		this.frame = 0;

		try {
			if (!loading) {
				int indicatorOffsetY = 300 - 54;

				for (String indicator : indicators) {
					try {
						NativeImage indicator_ = NativeImage.fromBase64(indicator);
						copyRect(indicator_, 0, 0, this.image, 4, indicatorOffsetY, 50, 50);
						indicatorOffsetY -= 50 + 4;
					}
					catch (IOException e) {
						Cosmetica.LOGGER.error("Error loading info indicator onto preview icon, {}", e);
					}
				}
			}

			this.upload(image, !loading);
		} catch (IllegalStateException e) {
			Cosmetica.LOGGER.error("Error while uploading icon texture (loading: {}, icon url: {})", loading, this.url);
			e.printStackTrace();
		}
	}

	public void upload(NativeImage image, boolean close) {
		TextureUtil.prepareImage(this.getId(), 0, image.getWidth(), this.frameHeight);
		image.upload(0, 0, 0, 0, this.frameHeight * this.frame, image.getWidth(), this.frameHeight, this.blur, false, false, close);
	}

	@Override
	public void tick() {
		if (this.frames > 1 && this.image != null && ((NativeImageAccessorMixin) (Object) this.image).getPixels() != 0) {
			this.tick = (this.tick + 1) % 2;

			if (this.tick == 0) {
				this.frame = (this.frame + 1) % this.frames;
				//Debug.info("Uploading frame {}", this.frame);
				this.upload(this.image, false);
			}
		}
	}

	@Override
	public void close() {
		//Debug.info("Closing image on thread {} due to dispose. Are we allowed? {}", Thread.currentThread(), RenderSystem.isOnRenderThreadOrInit());
		if (this.image != null) this.image.close();
		//Debug.info("Disposed of image.");
	}

	private static void copyRect(NativeImage src, int srcX, int srcY, NativeImage dest, int destX, int destY, int width, int height) {
		for(int dx = 0; dx < width; ++dx) {
			for(int dy = 0; dy < height; ++dy) {
				int colour = src.getPixelRGBA(srcX + dx, srcY + dy);
				dest.setPixelRGBA(destX + dx, destY + dy, colour);
			}
		}
	}
}
