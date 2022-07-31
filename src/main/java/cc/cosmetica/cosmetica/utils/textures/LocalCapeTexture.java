package cc.cosmetica.cosmetica.utils.textures;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.utils.Debug;
import cc.cosmetica.cosmetica.mixin.textures.NativeImageAccessorMixin;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.function.Supplier;

public class LocalCapeTexture extends AnimatedTexture implements Tickable {
	public LocalCapeTexture(ResourceLocation debugPath, Supplier<NativeImage> image) {
		this.imageSupplier = image;
		this.debugPath = debugPath;

		Debug.info("Uploading native texture {}", this.debugPath);

		this.image = this.imageSupplier.get();

		if (this.image == null) {
			Cosmetica.LOGGER.warn("Sorry texture machine broke for {}", this.debugPath);
		}
		else {
			this.setupAnimations();
		}
	}

	private final ResourceLocation debugPath;
	private final Supplier<NativeImage> imageSupplier;

	@Override
	protected void setupAnimations() throws IllegalStateException {
		super.setupAnimations();
		this.frameCounterTicks = Math.max(1, Debug.frameDelayMs / 50);
	}

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
		Debug.info("Re-uploading native texture {}", this.debugPath);
		this.image = this.imageSupplier.get();

		if (this.image == null) {
			Cosmetica.LOGGER.warn("Sorry texture machine broke for {}", this.debugPath);
		}
		else {
			this.setupAnimations();
			this.upload();
		}
	}

	@Override
	public void tick() {
		this.doTick();
	}
}
