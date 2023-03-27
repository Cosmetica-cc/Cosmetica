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

import cc.cosmetica.api.Box;
import cc.cosmetica.cosmetica.utils.collections.Stacc;
import cc.cosmetica.cosmetica.utils.collections.ArrayStacc;
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

		// if no test models just use existing hats
		if (this.testModels.isEmpty()) return existingHats;
		// if there are no existing hats, just use the test ones
		if (existingHats.isEmpty()) return List.of(this.testModels.peek());

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
