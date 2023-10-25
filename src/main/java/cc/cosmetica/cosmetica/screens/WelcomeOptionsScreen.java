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

package cc.cosmetica.cosmetica.screens;

import benzenestudios.sulphate.ExtendedScreen;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public class WelcomeOptionsScreen extends OptionsScreen {
	public WelcomeOptionsScreen(Screen screen, Options options) {
		super(screen, options);
		this.parent = screen;
		this.options = options;
	}

	private final Screen parent;
	private final Options options;

	@Override
	protected void init() {
		super.init();

		// yep, that's right
		// we're replacing the button thrice: once in options screen mixin, another time here
		// yes, there are probably better ways of doing this, but at least I need to do this loop anyway.
		Button removeMe = null;

		for (GuiEventListener widget : this.children()) {
			if (widget instanceof AbstractWidget) {
				AbstractWidget b = (AbstractWidget) widget;

				if (b instanceof Button && b.getMessage() instanceof TranslatableComponent && ((TranslatableComponent) b.getMessage()).getKey().equals("cosmetica.cosmetics")) {
					removeMe = (Button) b;
				}
				else {
					b.active = false;
				}
			}
		}

		((ExtendedScreen) this).removeChild(removeMe);

		this.addButton(new Button(this.width / 2 - 155, this.height / 6 + 48 - 6, 150, 20, new TranslatableComponent("cosmetica.cosmetics"),
				button -> this.minecraft.setScreen(new LoadingScreen(this.parent instanceof OptionsScreen ? this.parent : new OptionsScreen(this.parent, this.options), this.options, 3))));
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
}