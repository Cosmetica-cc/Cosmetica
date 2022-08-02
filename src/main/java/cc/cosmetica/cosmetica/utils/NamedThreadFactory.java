package cc.cosmetica.cosmetica.utils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
	public NamedThreadFactory(String name) {
		this.name = name;
	}

	private String name;
	private static AtomicInteger n = new AtomicInteger(1);

	@Override
	public Thread newThread(@NotNull Runnable r) {
		Thread thread = new Thread(r);
		thread.setName(this.name + " #" + n.getAndIncrement());
		return thread;
	}
}
