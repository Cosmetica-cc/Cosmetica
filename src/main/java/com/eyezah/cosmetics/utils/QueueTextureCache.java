package com.eyezah.cosmetics.utils;

import com.eyezah.cosmetics.cosmetics.model.ModifiableAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Queue-like texture cache. First-Come, First-Served.
 */
public class QueueTextureCache {
	/**
	 * @param size the size of the cache, must be a power of tool due to bitwise operations utilised (technically could use modulo instead but speed)
	 */
	public QueueTextureCache(int size) {
		this.size = size;
		this.indices = new ResourceLocation[size];
		this.sprites = new ModifiableAtlasSprite[size];
		this.used = new int[size];
	}

	private final int size;
	private final ResourceLocation[] indices; // the numbers index by a resource location
	private final ModifiableAtlasSprite[] sprites; // the sprite at an index

	// to limit the number of textures loaded and thus baked models created per frame, should this ever be necessary on a server
	long lastRenderTime; // default value = 0 should be fine
	final int[] used; // 0 = unused, 1 = used last frame, 2 = used this frame.
	int search = 0; // current search index for an unused space

	private int getIndex(ResourceLocation location) {
		for (int i = 0; i < this.size; ++i) {
			if (location.equals(this.indices[i])) {
				return i;
			}
		}

		return -1;
	}

	@Nullable
	public ModifiableAtlasSprite getAtlasSprite(ResourceLocation runtimeLocation, long renderTime) {
		if (renderTime != this.lastRenderTime) {
			this.lastRenderTime = renderTime;
			this.search = 0;

			for (int i = 0; i < this.size; ++i) {
				if (this.used[i] > 0) this.used[i]--;
			}
		}

		int index = this.getIndex(runtimeLocation);

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
			this.indices[index] = runtimeLocation;
			//this.sprites[index].setTexture(runtimeLocation); TODO
		}

		// mark it as being used
		this.used[index] = 2;
		return this.sprites[index];
	}
}