package com.eyezah.cosmetics.cosmetics.model;

import cc.cosmetica.api.Box;
import com.eyezah.cosmetics.utils.ArrayStacc;
import com.eyezah.cosmetics.utils.Stacc;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class CosmeticStack<T> {
	private Stacc<T> testModels = new ArrayStacc<>();
	private int index = -1;
	private boolean removeModel = false;

	private static CosmeticStack solo;
	private static final CosmeticStack<ResourceLocation> NO_COSMETICS = new CosmeticStack<>();

	public void setIndex(int index) {
		this.index = index;
	}

	@Nullable
	public T get(Supplier<T> orElse) {
		if (solo != null && solo != this) return null;
		return this.testModels.isEmpty() ? orElse.get() : this.peek();
	}

	public List<T> getList(Supplier<List<T>> orElse) {
		if (solo != null && solo != this) return new ArrayList<>();
		if (this.index > -1) return this.getVerySpecialListSpecificallyForHatsOnTheApplyCosmeticsScreen(orElse.get(), this.index);
		return this.testModels.isEmpty() ? orElse.get() : this.lPeek();
	}

	public void solo() {
		solo = this;
	}

	public boolean isSolo() {
		return solo == this;
	}

	public static void normal() {
		solo = null;
	}

	public static void strip() {
		solo = NO_COSMETICS;
	}

	private List<T> getVerySpecialListSpecificallyForHatsOnTheApplyCosmeticsScreen(List<T> existingHats, int replaceThisOne) {
		if (solo != null && solo != this) return new ArrayList<>();

		if (this.testModels.isEmpty()) return existingHats;

		List<T> useMeHats = new ArrayList<>(existingHats);

		if (replaceThisOne == 1 && useMeHats.size() < 2) {
			if (!this.removeModel) useMeHats.add(this.testModels.peek());
		}
		else {
			if (this.removeModel) {
				useMeHats.remove(replaceThisOne);
			} else {
				useMeHats.set(replaceThisOne, this.testModels.peek());
			}
		}

		return useMeHats;
	}

	public void push(T model) {
		this.testModels.push(model);
		this.removeModel = model == NO_RESOURCE_LOCATION || model == NO_BAKABLE_MODEL;
	}

	private T peek() {
		if (this.removeModel) return null;
		return this.testModels.peek();
	}

	private List<T> lPeek() {
		if (this.removeModel) return List.of();
		return List.of(this.testModels.peek());
	}

	public void clear() {
		this.testModels.clear();
		this.removeModel = false;
	}

	public void pop() {
		this.testModels.pop();
		this.removeModel = !this.testModels.isEmpty() && (this.testModels.peek() == NO_RESOURCE_LOCATION || this.testModels.peek() == NO_BAKABLE_MODEL);
	}

	public static final ResourceLocation NO_RESOURCE_LOCATION = new ResourceLocation("cosmetica", "none");
	public static final BakableModel NO_BAKABLE_MODEL = new BakableModel("none", "No Model", null, null, 0, new Box(0, 0, 0, 0, 0, 0));
}
