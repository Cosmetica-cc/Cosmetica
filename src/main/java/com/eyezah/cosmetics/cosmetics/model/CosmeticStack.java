package com.eyezah.cosmetics.cosmetics.model;

import com.eyezah.cosmetics.utils.ArrayStacc;
import com.eyezah.cosmetics.utils.Stacc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class CosmeticStack<T> {
	private Stacc<T> testModels = new ArrayStacc<>();
	private int index = -1;

	public void setIndex(int index) {
		this.index = index;
	}

	public void push(T model) {
		this.testModels.push(model);
	}

	@Nullable
	public T get(Supplier<T> orElse) {
		return this.testModels.isEmpty() ? orElse.get() : this.testModels.peek();
	}

	public List<T> getList(Supplier<List<T>> orElse) {
		if (this.index > -1) return this.getVerySpecialListSpecificallyForHatsOnTheApplyCosmeticsScreen(orElse.get(), this.index);
		return this.testModels.isEmpty() ? orElse.get() : List.of(this.testModels.peek());
	}

	private List<T> getVerySpecialListSpecificallyForHatsOnTheApplyCosmeticsScreen(List<T> existingHats, int replaceThisOne) {
		if (this.testModels.isEmpty()) return existingHats;

		List<T> useMeHats = new ArrayList<>(existingHats);

		if (replaceThisOne == 1 && useMeHats.size() < 2) {
			useMeHats.add(this.testModels.peek());
		}
		else {
			useMeHats.set(replaceThisOne, this.testModels.peek());
		}

		return useMeHats;
	}

	public void clear() {
		this.testModels.clear();
	}

	public void pop() {
		this.testModels.pop();
	}
}
