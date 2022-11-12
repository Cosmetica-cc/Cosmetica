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

package cc.cosmetica.cosmetica.screens;

import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.CustomCosmetic;
import cc.cosmetica.api.UserSettings;
import cc.cosmetica.cosmetica.cosmetics.model.CosmeticStack;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CustomiseCosmeticsScreen extends ViewCosmeticsScreen {
	public CustomiseCosmeticsScreen(Screen parentScreen, FakePlayer player, UserSettings settings) {
		this(parentScreen, player, new ServerOptions(settings), 1.0);
		this.canCloseWithBn = true;
	}

	protected CustomiseCosmeticsScreen(Screen parentScreen, FakePlayer player, ServerOptions options, double transitionProgress) {
		super(TextComponents.translatable("cosmetica.customizeCosmetics"), parentScreen, player, options, transitionProgress);
		this.leftMouseGrabBuffer = 51;
	}

	private boolean canCloseWithBn = false;

	public boolean canCloseWithBn() {
		return this.canCloseWithBn;
	}

	@Override
	protected <T extends CustomCosmetic, E> ViewCosmeticsScreen.Section createActiveSection(String title, List<String> items, @Nullable CosmeticType<T> type, @Nullable CosmeticStack<E> stack) {
		Button.OnPress onChange = type == null ? b -> this.minecraft.setScreen(new SelectLoreScreen(this, items.isEmpty() ? "" : items.get(0))) :
				b -> this.minecraft.setScreen(new BrowseCosmeticsScreen(this, type, stack));
		Div section = Div.create(title);
		Component headerText = TextComponents.translatable("cosmetica.entry." + title.replace(" ", ""));

		this.addTextTo(section, headerText, 200, false);

		if (items.isEmpty()) {
			this.addTextTo(section, TextComponents.translatable("cosmetica.entry.none"), 200, false).active = false;
		}
		else {
			for (String item : items) {
				this.addTextTo(section, TextComponents.literal(item), 200, false).active = false;
			}
		}

		section.addChild(new Button(0, 0, 100, 20, TextComponents.translatable("cosmetica.change"), onChange));
		section.calculateDimensions();
		return section;
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		this.renderRSENotif(matrices, mouseX, mouseY);
	}
}
