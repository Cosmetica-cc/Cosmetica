package cc.cosmetica.cosmetica.utils;

public interface Stacc<T> {
	T peek();
	void push(T item);
	boolean isEmpty();
	T pop();
	void clear();
}
