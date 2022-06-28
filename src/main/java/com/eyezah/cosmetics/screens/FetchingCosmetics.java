package com.eyezah.cosmetics.screens;

import cc.cosmetica.api.ServerResponse;
import com.eyezah.cosmetics.utils.TextComponents;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class FetchingCosmetics<T> extends TextWidget {
	private int number = 1;

	protected FetchingCosmetics(String entry, Supplier<List<ServerResponse<T>>> fetcher, BiConsumer<FetchingCosmetics, List<T>> onLoad) {
		super(0, 0, 200, 20, true, TextComponents.translatable("cosmetica.selection.fetch").append(TextComponents.translatable("cosmetica.entry." + entry)));

		Thread t = new Thread(() -> {
			List<ServerResponse<T>> responses = fetcher.get();

			List<T> toPost = new ArrayList<>(responses.size());
			Exception e;

			for (ServerResponse<T> response : responses) {
				if ((e = response.getException()) != null) {
					e.printStackTrace();
					onLoad.accept(this, ImmutableList.of());
					return;
				} else {
					toPost.add(response.get());
				}
			}

			onLoad.accept(this, toPost);
		});
		t.setName("Fetching Cosmetics Thread #" + (this.number++));
		t.start();
	}
}
