/*
 * Copyright 2022, 2023 EyezahMC
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

import cc.cosmetica.api.Box;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class BakableModel {
	private final String id;
	private final String name;
	private final BlockModel model;
	private final ResourceLocation image;
	private final int extraInfo;
	private final Box bounds;

	public BakableModel(String id, String name, BlockModel model, ResourceLocation image, int extraInfo, Box bounds) {
		this.id = id;
		this.name = name;
		this.model = model;
		this.image = image;
		this.extraInfo = extraInfo;
		this.bounds = bounds;
	}

	public String id() {
		return id;
	}

	public String name() {
		return name;
	}

	public BlockModel model() {
		return model;
	}

	public ResourceLocation image() {
		return image;
	}

	public int extraInfo() {
		return extraInfo;
	}

	public Box bounds() {
		return bounds;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		BakableModel that = (BakableModel) obj;
		return Objects.equals(this.id, that.id) &&
				Objects.equals(this.name, that.name) &&
				Objects.equals(this.model, that.model) &&
				Objects.equals(this.image, that.image) &&
				this.extraInfo == that.extraInfo &&
				Objects.equals(this.bounds, that.bounds);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, model, image, extraInfo, bounds);
	}

	@Override
	public String toString() {
		return "BakableModel[" +
				"id=" + id + ", " +
				"name=" + name + ", " +
				"model=" + model + ", " +
				"image=" + image + ", " +
				"extraInfo=" + extraInfo + ", " +
				"bounds=" + bounds + ']';
	}
}
