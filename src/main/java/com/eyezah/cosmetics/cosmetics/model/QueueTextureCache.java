package com.eyezah.cosmetics.cosmetics.model;

import com.eyezah.cosmetics.mixin.MixinTextureAtlasSpriteInvoker;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.io.File;
import java.io.IOException;

/**
 * Queue-like texture cache. First-Come, First-Served.
 */
public class QueueTextureCache {
	/**
	 * @param size the size of the cache, must be a power of tool due to bitwise operations utilised (technically could use modulo instead but speed)
	 */
	public QueueTextureCache(int size) {
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
	final int[] used; // 0 = unused, 1 = used last tick, 2 = used this tick.
	int search = 0; // current search index for an unused space

	public void addAtlasSprite(TextureAtlasSprite result) {
		this.sprites[this.emptySpriteIndex++] = result;
	}

	void clear() {
		for (int i = 0; i < this.size; ++i) {
			this.used[i] = 0;
			this.ids[i] = null;
		}
	}

	// should be called from the render thread because of texture setting probably
	// andThen may not be called on the same thread -- proceed with caution
	public TextureAtlasSprite getAtlasSprite(BakableModel model, long tickTime) {
		if (tickTime != this.lastTickTime) {
			this.lastTickTime = tickTime;
			this.search = 0;

			for (int i = 0; i < this.size; ++i) {
				if (this.used[i] > 0) this.used[i]--;
			}
		}

		int index = this.getIndex(model.id());

		if (index == -1) {
			if (this.search == this.size) return null;
			index = this.search;

			while (this.used[index] > 0) {
				// increment. if reached the end, cannot load any new textures
				if (++index == this.size) { // attention code editors: keep the ++ operator before index! ++index returns the result after incrementing, whereas index++ returns the result before!
					this.search = this.size;
					return null;
				}
			}

			//System.out.println("Using New Index: " + index);
			//System.out.println("Count: " + ((MixinTextureAtlasSpriteInvoker)this.sprites[index]).getMainImage().length); Count: 5
			// at this point, index is guaranteed to be a value which is free
			this.search = index + 1; // the next spot over

			// use this index of reserved texture
			// remove existing associated model
			if (this.ids[index] != null) Models.removeBakedModel(this.ids[index]);
			// upload new model
			this.ids[index] = model.id();
			MixinTextureAtlasSpriteInvoker sprite = ((MixinTextureAtlasSpriteInvoker)this.sprites[index]);
			NativeImage[] mipmap = MipmapGenerator.generateMipLevels(model.image(), sprite.getMainImage().length);

//			for (int i = 0; i < 5; ++i) {
//				File test = new File(FabricLoader.getInstance().getGameDirectory(), "reserved_" + index + "_dump.png");
//				File test = new File(FabricLoader.getInstance().getGameDirectory(), "existing_" + index + "_dump_" + i + ".png");
//				File test = new File(FabricLoader.getInstance().getGameDirectory(), "reserved_" + index + "_dump_" + i + ".png");
//				try {
//					test.createNewFile();
//					//model.image()[0].writeToFile(test);
//					//((MixinTextureAtlasSpriteInvoker) this.sprites[index]).getMainImage()[i].writeToFile(test);
//					mipmap[i].writeToFile(test);
//				} catch (IOException e) {
//					throw new RuntimeException(e);
//				}
//			}
			sprite.callUpload(0, 0, mipmap);
		}

		// mark it as being used
		this.used[index] = 2;
		return this.sprites[index];
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
}
