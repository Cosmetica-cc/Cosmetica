package com.eyezah.cosmetics.cosmetics.model;

import com.eyezah.cosmetics.mixin.MixinTextureAtlasSpriteInvoker;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.Scheduler;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.function.Consumer;

/**
 * Queue-like texture cache. First-Come, First-Served.
 */
public class RuntimeTextureManager {
	/**
	 * @param size the size of the cache, must be a power of tool due to bitwise operations utilised (technically could use modulo instead but speed)
	 */
	public RuntimeTextureManager(int size) {
		this.size = size;
		this.ids = new String[size];
		this.sprites = new TextureAtlasSprite[size];
		this.used = new int[size];
	}

	private final int size;
	private final String[] ids; // the numbers index by a resource location
	private final TextureAtlasSprite[] sprites; // the sprite at an index
	private int emptySpriteIndex = 0;

	// to limit the number of textures loaded and thus baked models created per tick, should this ever be necessary on a server
	long lastTickTime; // default long value = 0 should be fine
	final int[] used; // 0 = unused, 1 = used last tick, 2 = used this tick, 3 - MAX_VALUE = searching
	int search = 0; // current search index for an unused space

	public void addAtlasSprite(TextureAtlasSprite result) {
		this.sprites[this.emptySpriteIndex] = result;
		this.emptySpriteIndex = (this.emptySpriteIndex + 1) & (this.size - 1);
	}

	void clear() {
		this.search = 0;

		for (int i = 0; i < this.size; ++i) {
			this.used[i] = 0;
			this.ids[i] = null;
		}
	}

	// should be called from the render thread because of texture setting probably
	// andThen may not be called on the same thread -- proceed with caution
	public void retrieveAllocatedSprite(BakableModel model, long tickTime, Consumer<TextureAtlasSprite> callback) {
		if (tickTime != this.lastTickTime) {
			this.lastTickTime = tickTime;
			this.search = 0;

			for (int i = 0; i < this.size; ++i) {
				if (this.used[i] > 0) this.used[i]--;
			}
		}

		int index = this.getIndex(model.id());

		if (index == -1) {
			if (this.search == this.size) return;
			index = this.search;

			while (this.used[index] > 0) {
				// increment. if reached the end, cannot load any new textures
				if (++index == this.size) { // attention code editors: keep the ++ operator before index! ++index returns the result after incrementing, whereas index++ returns the result before!
					this.search = this.size;
					return;
				}
			}

			Debug.info("Using New Index: " + index);
			//System.out.println("Count: " + ((MixinTextureAtlasSpriteInvoker)this.sprites[index]).getMainImage().length); Count: 5
			// at this point, index is guaranteed to be a value which is free
			this.search = index + 1; // the next spot over

			// use this index of reserved texture
			// remove existing associated model
			if (this.ids[index] != null) Models.removeBakedModel(this.ids[index]);
			// upload new model
			this.ids[index] = model.id();
			this.used[index] = Integer.MAX_VALUE; // basically indefinitely marking it as unuseable

			TextureAtlasSprite sprite = this.sprites[index];
			final int index_ = index;

			Scheduler.scheduleTask(Scheduler.Location.TEXTURE_TICK, () -> {
				NativeImage[] mipmap = MipmapGenerator.generateMipLevels(model.image(), ((MixinTextureAtlasSpriteInvoker) sprite).getMainImage().length - 1);
				Debug.info(() -> {
					NativeImage[] oldMipmap = ((MixinTextureAtlasSpriteInvoker) sprite).getMainImage();
					ImageDebugInfo oldDebugInfo = new ImageDebugInfo(oldMipmap);
					ImageDebugInfo newDebugInfo = new ImageDebugInfo(mipmap);
					return "Allocating Sprite: " + sprite.getName() + ". OldImg: " + oldDebugInfo + " | NewImg: " + newDebugInfo;
				});
				Debug.dumpImages(sprite.getName().toDebugFileName() + "_old", ((MixinTextureAtlasSpriteInvoker) sprite).getMainImage());
				Debug.dumpImages(sprite.getName().toDebugFileName(), mipmap);
				((MixinTextureAtlasSpriteInvoker) sprite).callUpload(0, 0, mipmap);
				//sprite.uploadFirstFrame();
				this.used[index_] = 2;
				System.out.println(Thread.currentThread().getName());
				callback.accept(sprite);
			});
		}

		// mark it as being used
		this.used[index] = 2;
		callback.accept(this.sprites[index]);
	}

	// literally just search the entire array to see if it exists
	// don't sort the array so probably the fastest way aside from creating and calling a native method with JNI which is overkill
	private int getIndex(String id) {
		for (int i = 0; i < this.size; ++i) {
			if (id.equals(this.ids[i])) {
				return i;
			}
		}

		return -1;
	}

	// Debug Stuff

	private record ImageDebugInfo(int mipmapLevels, String dimensions, String allocInfo, NativeImage.Format format, int glFormatNum, int formatComponents) {
		ImageDebugInfo(NativeImage[] mipmap) {
			this(mipmap.length, mipmap[0].getWidth() + "x" + mipmap[0].getHeight(),
					((NIDebugInfoProvider) (Object) mipmap[0]).getAllocType().toString() + (((NIDebugInfoProvider) (Object) mipmap[0]).usesStbFree() ? "_S" : "_N"),
					mipmap[0].format(), mipmap[0].format().glFormat(), mipmap[0].format().components());
		}
	}

	public enum PixelAllocType {
		PROVIDED,
		CALLOC,
		ALLOC
	}

	public interface NIDebugInfoProvider {
		boolean usesStbFree();
		PixelAllocType getAllocType();
	}
}
