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

import benzenestudios.sulphate.Anchor;
import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.UserSettings;
import cc.cosmetica.cosmetica.utils.TextComponents;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class IconSettingsScreen extends SulphateScreen {
	public IconSettingsScreen(Screen parent, ServerOptions options) {
		super(TextComponents.translatable("cosmetica.icons"), parent);
		this.butler = options;

		this.setAnchorY(Anchor.TOP, () -> this.height / 6);
	}

	// the butler serves server options
	private final ServerOptions butler;
	private final List<Button> childButtons = new ArrayList<>();

	@Override
	protected void addWidgets() {
		this.childButtons.clear();

		this.addButton("cosmetica.doIcons", UserSettings.DISABLE_ICONS, true);
		this.addButton("cosmetica.doOfflineIcons", UserSettings.DISABLE_OFFLINE_ICONS, false);
		this.addButton("cosmetica.doSpecialIcons", UserSettings.DISABLE_SPECIAL_ICONS, false);

		this.addDone();

		updateEnabledButtons();
	}

	private void updateEnabledButtons() {
		final boolean shouldChildrenBeActive = !this.butler.icons.get(UserSettings.DISABLE_ICONS);

		for (Button button : this.childButtons) {
			button.active = shouldChildrenBeActive;
		}
	}

	void addButton(String translationKey, int flag, boolean master) {
		Button button = this.addButton(generateIconButtonToggleText(translationKey, this.butler.icons, flag), button_ -> {
			this.butler.icons.toggle(flag);
			button_.setMessage(generateIconButtonToggleText(translationKey, this.butler.icons, flag));

			if (master) {
				this.updateEnabledButtons();
			}
		}, (button__, poseStack, i, j) -> {
			if (!master && !button__.active) this.renderTooltip(poseStack, this.font.split(DISABLE_NOTICE, Math.max(this.width / 2 - 43, 170)), i, j + 18);
		});

		if (!master) {
			this.childButtons.add(button);
		}
	}

	static TextComponent generateIconButtonToggleText(String translationKey, MultiOption option, int flag) {
		TextComponent component = new TextComponent("");
		component.append(TextComponents.translatable(translationKey));
		component.append(": ");

		if (!option.get(flag)) {
			component.append(TextComponents.translatable("cosmetica.enabled"));
		} else {
			component.append(TextComponents.translatable("cosmetica.disabled"));
		}

		return component;
	}

	private static final Component DISABLE_NOTICE = TextComponents.translatable("cosmetica.doIcons.disableNotice");
}
