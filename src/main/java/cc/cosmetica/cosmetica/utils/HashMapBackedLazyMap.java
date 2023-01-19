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

import com.mojang.datafixers.util.Either;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Lazy map backed by a hash map.
 */
public class HashMapBackedLazyMap<K, V> implements LazyMap<K, V> {
	public HashMapBackedLazyMap() {
		this.map = new HashMap<>();
	}

	private final Map<K, Either<Supplier<V>, V>> map;
	private int size;

	@Override
	public void put(K key, Supplier<V> value) {
		this.map.put(key, Either.left(value));
	}

	@Override
	public V get(K key) {
		Either<Supplier<V>, V> item = this.map.get(key);

		if (item.left().isPresent()) {
			V value = item.left().get().get();
			this.map.put(key, Either.right(value));
			this.size++;
			return value;
		}
		else {
			return item.right().get();
		}
	}

	@Override
	public boolean containsKey(K key) {
		return this.map.containsKey(key);
	}

	@Override
	public boolean isEvaluated(K key) {
		return this.map.containsKey(key) && this.map.get(key).right().isPresent();
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public int capacity() {
		return this.map.size();
	}
}
