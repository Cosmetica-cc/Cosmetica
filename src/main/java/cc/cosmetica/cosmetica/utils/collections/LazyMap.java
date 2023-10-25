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

package cc.cosmetica.cosmetica.utils.collections;

import java.util.function.Supplier;

/**
 * A map that stores key-value pairs as suppliers, constructing the objects only when necessary.
 * @param <K> the key type.
 * @param <V> the value type.
 */
public interface LazyMap<K, V> {
	/**
	 * Put a lazy-supplied value in this map.
	 * @param key
	 * @param value
	 */
	void put(K key, Supplier<V> value);

	/**
	 * Gets the value for the given key.
	 * @param key the key to get a value for.
	 * @return the value associated with this key.
	 */
	V get(K key);

	/**
	 * Returns whether the map currently contains a mapping for the given key. This includes suppliers of the value that haven't been evaluated.
	 * @param key the key to test.
	 * @return whether there is a mapping for the given key, including unresolved suppliers.
	 */
	boolean containsKey(K key);

	/**
	 * Returns whether the map currently contains an evaluated mapping for the given key.
	 * @param key the key to test.
	 * @return whether the map contains a mapping at the given key, and the value for that mapping is evaluated.
	 */
	boolean isEvaluated(K key);

	/**
	 * Gets the size of this lazy map. This includes only resolved objects.
	 * @return the number of objects stored in the map currently. This includes only objects for which the supplier has been evaluated.
	 */
	int size();

	/**
	 * Gets the current storage capacity of the lazy map.
	 * @return the storage capacity of this lazy map. That is: the number of fully evaluated objects it would carry if all entries were fully evaluated.
	 */
	int capacity();
}
