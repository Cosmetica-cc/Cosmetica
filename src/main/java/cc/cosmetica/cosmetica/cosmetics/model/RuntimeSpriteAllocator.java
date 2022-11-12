/*
 * Copyright 2022 EyezahMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.cosmetica.cosmetica.cosmetics.model;

import cc.cosmetica.cosmetica.mixin.textures.TextureAtlasSpriteInvokerMixin;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.utils.Debug;
import cc.cosmetica.cosmetica.utils.Scheduler;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.function.Consumer;

/**
 * Queue-like texture cache. First-Come, First-Served.
 */
public class RuntimeSpriteAllocator {
	/**
	 * @param size the size of the cache, must be a power of 2 due to bitwise operations utilised (technically could use modulo instead but speed)
	 */
	public RuntimeSpriteAllocator(int size) {
		this.size = size;
		this.ids = new String[size];
		this.sprites = new TextureAtlasSprite[size];
		this.used = new int[size];
		this.cooldown = new boolean[size];
		Scheduler.scheduleRepeatable(Scheduler.Location.TEXTURE_TICK, this::tick);
	}

	private final int size;
	private final String[] ids; // the numbers index by a resource location
	private final TextureAtlasSprite[] sprites; // the sprite at an index
	private int emptySpriteIndex = 0;

	// to limit the number of textures loaded and thus baked models created per tick, should this ever be necessary on a server
	long lastTickTime; // default long value = 0 should be fine
	final int[] used; // 0 = unused, 1 = used last tick, 2 = used this tick, 3 - MAX_VALUE = searching
	final boolean[] cooldown; // Whether the sprite was null at allocation
	int search = 0; // current search index for an unused space (reset each tick)

	public void addAtlasSprite(TextureAtlasSprite result) {
		// minecraft loads resources in parallel
		synchronized (this) {
			Debug.info("Adding Atlas Sprite {} at index {}", result, this.emptySpriteIndex);
			this.sprites[this.emptySpriteIndex] = result;
			this.emptySpriteIndex = (this.emptySpriteIndex + 1) & (this.size - 1);
		}
	}

	void clear() {
		this.search = 0;

		for (int i = 0; i < this.size; ++i) {
			this.used[i] = 0;
			this.ids[i] = null;
		}
	}

	void tick() {
		this.search = 0;

		for (int i = 0; i < this.size; ++i) {
			if (this.used[i] > 0) {
				this.used[i]--; // once it reaches zero it's not gonna be overwritten just yet, but it will be marked as able to be overwritten. So if it needs the space it will overwrite it.

				// Detect if we need to force it to try again for the given ID (after the fabled 20 ticks)
				if (this.used[i] == 0 && this.cooldown[i]) {
					Cosmetica.LOGGER.info("Preparing to try assign a sprite for {} again...", this.ids[i]);
					this.cooldown[i] = false;
					Models.removeBakedModel(this.ids[i]);
					this.ids[i] = null;
				}
			}
		}
	}

	// should be called from the render thread because of texture setting probably
	// callback may not be called on the same thread -- proceed with caution
	public void retrieveAllocatedSprite(BakableModel model, Consumer<TextureAtlasSprite> callback) {
		int index = this.getIndex(model.id());

		if (index == -1) {
			if (this.search == this.size) return;
			index = this.search;

			while (this.used[index] > 0) {
				// increment. if reached the end, cannot load any new textures
				if (++index == this.size) { // attention code editors: keep the ++ operator before index! ++index returns the result after incrementing, whereas index++ returns the result before!
					this.search = this.size;
					return; // return silently. we ran out of space. no major worry.
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

			if (sprite == null) { // this should not happen, however it does seem to be happening sometimes if Iris is installed, so here's a catch to not destroy the game and give the game some time.
				Cosmetica.LOGGER.error("The sprite assigned to model {} is null! Will try again in 20 ticks.", model.id());
				Cosmetica.LOGGER.error("Relevant Debug Info: model.id()={}, emptySpriteIndex={}, allocatedIndex={}", model.id(), this.emptySpriteIndex, index);
				this.used[index] = 20;
				this.cooldown[index] = true;
				return; // don't run code that requires it
			}

			final int index_ = index;

			Scheduler.scheduleTask(Scheduler.Location.TEXTURE_TICK, () -> {
				// generate mipmap
				NativeImage[] mipmap = MipmapGenerator.generateMipLevels(model.image(), ((TextureAtlasSpriteInvokerMixin) sprite).getMainImage().length - 1);
				Debug.info("Allocating Sprite: " + sprite.getName());
				Debug.dumpImages(sprite.getName().toString().replace(':', '-') + "_old", false, ((TextureAtlasSpriteInvokerMixin) sprite).getMainImage());
				Debug.dumpImages(sprite.getName().toString().replace(':', '-'), false, mipmap);
				// bind the texture
				GlStateManager._bindTexture(((TextureAtlasSpriteInvokerMixin) sprite).getAtlas().getId());
				// upload to the texture
				((TextureAtlasSpriteInvokerMixin) sprite).callUpload(0, 0, mipmap);
				//sprite.uploadFirstFrame();

				// hack to make it set this later
				this.used[index_] = 2;
				callback.accept(sprite);
			});
		}

		TextureAtlasSprite sprite = this.sprites[index];

		if (sprite != null) {
			// mark it as still being used
			this.used[index] = 2;
			callback.accept(sprite);
		}
	}

	public void markStillUsingSprite(BakableModel model) {
		int index = this.getIndex(model.id());

		if (index != -1) {
			// Ensure that's not in the cooldown phase before resetting the unused-cooldown
			if (!this.cooldown[index]) {
				this.used[index] = 2;
			}
		}
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("RuntimeTextureManager");

		for (int i = 0; i < this.size; ++i) {
			if (this.used[i] > 0) {
				sb.append("[u:").append(this.used[i]).append(",k:").append(this.ids[i]).append("]");
			}
		}

		return sb.toString();
	}
}
