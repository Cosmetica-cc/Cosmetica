package com.eyezah.cosmetics.utils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

public class NamedSingleThreadFactory implements ThreadFactory {
	public NamedSingleThreadFactory(String name) {
		this.name = name;
	}

	private String name;

	@Override
	public Thread newThread(@NotNull Runnable r) {
		Thread thread = new Thread(r);
		thread.setName(this.name);
		return thread;
	}
}
