package com.eyezah.cosmetics.utils;

import java.util.ArrayDeque;
import java.util.Queue;

public class Scheduler {
	public static void executeScheduledTasks(Location location) {
		int i = location.tasks.size();

		while (i --> 0) {
			location.tasks.remove().run();
		}
	}

	public static void scheduleTask(Location location, Runnable r) {
		location.tasks.add(r);
	}

	public enum Location {
		TEXTURE_TICK;

		private final Queue<Runnable> tasks = new ArrayDeque<>();
	}
}
