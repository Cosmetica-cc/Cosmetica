package cc.cosmetica.cosmetica.cosmetics.model;

import cc.cosmetica.api.Box;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.block.model.BlockModel;

import java.util.Objects;

public final class BakableModel {
	private final String id;
	private final String name;
	private final BlockModel model;
	private final NativeImage image;
	private final int extraInfo;
	private final Box bounds;

	BakableModel(String id, String name, BlockModel model, NativeImage image, int extraInfo, Box bounds) {
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

	public NativeImage image() {
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
