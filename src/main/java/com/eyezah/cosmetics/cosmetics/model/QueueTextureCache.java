package com.eyezah.cosmetics.cosmetics.model;

import com.eyezah.cosmetics.mixin.MixinTextureAtlasSpriteInvoker;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

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
				index++;

				// if reached the end and cannot load any new textures
				if (index == this.size) {
					this.search = this.size;
					return null;
				}
			}

			// at this point, index is guaranteed to be a value which is free
			this.search = index + 1; // the next spot over

			// use this index of reserved texture
			// remove existing associated model
			if (this.ids[index] != null) Models.removeBakedModel(this.ids[index]);
			// upload new model
			this.ids[index] = model.id();
			((MixinTextureAtlasSpriteInvoker)this.sprites[index]).callUpload(0, 0, new NativeImage[]{model.image()});
		}

		// mark it as being used
		this.used[index] = 2;
		return this.sprites[index];
	}

	private int getIndex(String id) {
		for (int i = 0; i < this.size; ++i) {
			if (id.equals(this.ids[i])) {
				return i;
			}
		}

		return -1;
	}
}