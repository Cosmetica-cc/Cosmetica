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

package cc.cosmetica.cosmetica.utils;

import java.util.function.IntFunction;

/**
 * Fast cache that shortens the number of possible hash values via using an `original & mask` algorithm, and uses an array lookup.
 * Keep in mind that the implementation is built for speed, and as such it does not prioritise removing the last referenced object first.
 */
public class FastCache<T> {
	/**
	 * @param size the size of the cache. Must be a power of 2!
	 * @param operator the operation to cache.
	 */
	public FastCache(int size, IntFunction<T[]> generator, Operator<T> operator) {
		this.operator = operator;
		this.mask = size - 1;
		this.keys = new Object[size];
		this.values = generator.apply(size);
	}

	private final Operator<T> operator;

	private int mask;
	private Object[] keys;
	private T[] values;

	public T sample(Object key) {
		int loc = key.hashCode() & this.mask;

		if (this.keys[loc] != key) {
			this.keys[loc] = key;
			return this.values[loc] = this.operator.sample(key);
		} else {
			return this.values[loc];
		}
	}

	@FunctionalInterface
	public interface Operator<T> {
		T sample(Object key);
	}
}