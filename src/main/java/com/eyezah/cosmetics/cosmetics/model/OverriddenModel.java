package com.eyezah.cosmetics.cosmetics.model;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class OverriddenModel {
	public OverriddenModel() {
		instances.add(this);
	}

	@Nullable
	private BakableModel bakableModel;
	private boolean replace;

	public void setReplacedModel(BakableModel model) {
		if (currentOverride != null) {
			currentOverride.bakableModel = null;
			currentOverride.replace = false;
		}

		currentOverride = this;
		this.bakableModel = model;
		this.replace = true;
	}

	@Nullable
	public BakableModel get(Supplier<BakableModel> orElse) {
		if (this.replace) {
			return this.bakableModel;
		} else if (currentOverride != null) {
			return null;
		} else {
			return orElse.get();
		}
	}

	public static void disable() {
		currentOverride = null;

		for (OverriddenModel instance : instances) {
			instance.bakableModel = null;
			instance.replace = false;
		}
	}

	private static List<OverriddenModel> instances = new ArrayList<>(2);
	@Nullable
	private static OverriddenModel currentOverride;
}
