package com.eyezah.cosmetics.utils;

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