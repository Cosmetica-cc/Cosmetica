package com.eyezah.cosmetics.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class Scheduler {
	private static final AtomicInteger TASK_NUMBER = new AtomicInteger(0);

	public static void executeScheduledTasks(Location location) {
		int i = location.tasks.size();

		while (i --> 0) {
			location.tasks.remove().run();
		}

		synchronized (location.repeatables) {
			for (Runnable repeatableTask : location.repeatables.values()) {
				repeatableTask.run();
			}
		}
	}

	public static void scheduleTask(Location location, Runnable r) {
		location.tasks.add(r);
	}

	/**
	 * Schedule a task to repeat at the specified location.
	 * @param location the location at which to run the task.
	 * @param r the task to run.
	 * @return a numerical id associated with your task.
	 */
	public static int scheduleRepeatable(Location location, Runnable r) {
		int tn = TASK_NUMBER.getAndIncrement();
		synchronized (location.repeatables) {
			location.repeatables.put(tn, r);
		}
		return tn;
	}

	public static void clearRepeatable(Location location, int taskId) {
		synchronized (location.repeatables) {
			location.repeatables.remove(taskId);
		}
	}

	public enum Location {
		TEXTURE_TICK;

		private final Queue<Runnable> tasks = new ArrayDeque<>();
		private final Int2ObjectMap<Runnable> repeatables = new Int2ObjectLinkedOpenHashMap<>();
	}
}
