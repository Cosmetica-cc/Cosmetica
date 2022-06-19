package com.eyezah.cosmetics.cosmetics.model;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class OverriddenModel {
	public OverriddenModel() {
		instances.add(this);
	}

	// the replaced model -- only this model will render
	@Nullable
	private BakableModel debugModel;
	// test "soft replaced" model. this model will render at all times for its type unless a 'hard replace' (debug model) is currently in session
	@Nullable
	private BakableModel testModel;

	public void setDebugModel(BakableModel model) {
		if (currentOverride != null) {
			currentOverride.debugModel = null;
		}

		currentOverride = this;
		this.debugModel = model;
	}

	public void setTestModel(BakableModel model) {
		this.testModel = model;
	}

	@Nullable
	public BakableModel get(Supplier<BakableModel> orElse) {
		if (this.debugModel != null) {
			return this.debugModel;
		} else if (currentOverride != null) {
			return null;
		} else {
			return this.testModel == null ? orElse.get() : this.testModel;
		}
	}

	@Nullable
	public List<BakableModel> getList(Supplier<List<BakableModel>> orElse) {
		if (this.debugModel != null) {
			return List.of(this.debugModel);
		} else if (currentOverride != null) {
			return null;
		} else {
			return this.testModel == null ? orElse.get() : List.of(this.testModel);
		}
	}

	public void removeTestModel() {
		this.testModel = null;
	}

	public static void disableDebugModels() {
		currentOverride = null;

		for (OverriddenModel instance : instances) {
			instance.debugModel = null;
		}
	}

	private static List<OverriddenModel> instances = new ArrayList<>(2);
	@Nullable
	private static OverriddenModel currentOverride;
}
