package com.eyezah.cosmetics.cosmetics.model;

import com.eyezah.cosmetics.utils.ArrayStacc;
import com.eyezah.cosmetics.utils.Stacc;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public final class CosmeticStack<T> {
	private Stacc<T> testModels = new ArrayStacc<>();

	public void push(T model) {
		this.testModels.push(model);
	}

	@Nullable
	public T get(Supplier<T> orElse) {
		return this.testModels.isEmpty() ? orElse.get() : this.testModels.peek();
	}

	public List<T> getList(Supplier<List<T>> orElse) {
		return this.testModels.isEmpty() ? orElse.get() : List.of(this.testModels.peek());
	}

	public List<T> getVerySpecialListSpecificallyForHatsOnTheApplyCosmeticsScreen(List<T> existingHats, int replaceThisOne) {
		if (this.testModels.isEmpty()) return existingHats;

		List<T> useMeHats = List.copyOf(existingHats);

		if (replaceThisOne == 2 && useMeHats.size() < 2) {
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
