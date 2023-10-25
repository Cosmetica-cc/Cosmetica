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

package cc.cosmetica.cosmetica.screens.widget;

import cc.cosmetica.api.ServerResponse;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class FetchingCosmetics<T> extends TextWidget {
	private int number = 1;

	public FetchingCosmetics(String entry, Supplier<List<ServerResponse<T>>> fetcher, BiConsumer<FetchingCosmetics, List<T>> onLoad) {
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

		this.setWidth(Minecraft.getInstance().font.width(this.getMessage()));
	}
}
