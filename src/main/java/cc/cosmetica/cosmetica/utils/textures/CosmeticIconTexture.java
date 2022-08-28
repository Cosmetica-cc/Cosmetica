package cc.cosmetica.cosmetica.utils.textures;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.mixin.textures.NativeImageAccessorMixin;
import cc.cosmetica.cosmetica.utils.Debug;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class CosmeticIconTexture extends HttpTexture implements Tickable {
	public CosmeticIconTexture(@Nullable File file, String url) {
		super(file, url, new ResourceLocation("cosmetica", "textures/gui/loading.png"), false, null);
		this.url = url;
		this.indicators = new HashSet<>();
	}

	private int frameHeight;
	private int frames;
	private int frame;
	private int tick;
	private NativeImage image;
	private final String url;
	private final Set<String> indicators;

	public void firstUpload(NativeImage image, boolean loading) {
		Debug.info("Uploading image: {}", loading ? "[loading.png]" : this.url);

		// memory management
		if (this.image != null && ((NativeImageAccessorMixin)(Object)this.image).getPixels() != 0L) {
			Debug.info("Closing image on thread {} due to load. Are we allowed? {}", Thread.currentThread(), RenderSystem.isOnRenderThreadOrInit());
			this.image.close();
		}

		this.image = image;

		this.frames = image.getHeight() / image.getWidth();
		this.frameHeight = image.getHeight() / this.frames;
		this.frame = 0;

		try {
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
