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

package cc.cosmetica.cosmetica.utils;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MutableOptionalFloat {
	private MutableOptionalFloat() {
		this.isPresent = false;
	}

	private MutableOptionalFloat(float value) {
		this.isPresent = true;
		this.value = value;
	}

	private float value;
	private boolean isPresent;

	public float get() throws NullPointerException {
		if (!this.isPresent) {
			throw new NullPointerException("No value present.");
		}

		return this.value;
	}

	public float orElse(float value) {
		if (!this.isPresent) {
			this.isPresent = true;
			this.value = value;
		}

		return this.value;
	}

	public float orElseGet(FloatSupplier supplier) {
		if (!this.isPresent) {
			this.isPresent = true;
			this.value = supplier.get();
		}

		return this.value;
	}

	public MutableOptionalFloat computeIfAbsent(Supplier<@Nullable Float> ifAbsent) {
		if (!this.isPresent) {
			@Nullable Float newValue = ifAbsent.get();

			if (newValue != null) {
				this.isPresent = true;
				this.value = newValue;
			}
		}

		return this;
	}

	public static MutableOptionalFloat of(float f) {
		return new MutableOptionalFloat(f);
	}

	public static MutableOptionalFloat empty() {
		return new MutableOptionalFloat();
	}

	@FunctionalInterface
	public interface FloatSupplier {
		float get();
	}
}
