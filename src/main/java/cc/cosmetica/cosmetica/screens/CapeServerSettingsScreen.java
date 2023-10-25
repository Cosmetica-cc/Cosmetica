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

import benzenestudios.sulphate.ClassicButton;
import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.CapeDisplay;
import cc.cosmetica.api.CapeServer;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.screens.widget.ButtonList;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CapeServerSettingsScreen extends SulphateScreen {
	protected CapeServerSettingsScreen(Screen parent, Map<String, CapeDisplay> settings, List<Map.Entry<String, CapeServer>> settingsForButtons) {
		super(TextComponents.translatable("cosmetica.capeServerSettings"), parent);

		this.originalSettings = settings;
		this.settings = new HashMap<>(settings);
		this.settingsForButtons = settingsForButtons;
	}

	private final List<Map.Entry<String, CapeServer>> settingsForButtons;
	private final Map<String, CapeDisplay> settings;
	private final Map<String, CapeDisplay> originalSettings;

	@Override
	protected void addWidgets() {
		ButtonList list = new ButtonList(this.minecraft, this, 25);

		this.settingsForButtons.forEach(entry ->
			list.addButton(200, TextComponents.literal(entry.getValue().getName() + ": ").append(TextComponents.translatable("cosmetica.capeDisplay." + this.settings.get(entry.getKey()).toString().toLowerCase(Locale.ROOT))), b -> {
				int newValue = this.settings.get(entry.getKey()).id + 1;
				if (newValue > 2) newValue = 0;
				this.settings.put(entry.getKey(), CapeDisplay.byId(newValue));

				b.setMessage(TextComponents.literal(entry.getValue().getName() + ": ").append(TextComponents.translatable("cosmetica.capeDisplay." + this.settings.get(entry.getKey()).toString().toLowerCase(Locale.ROOT))));
			}, entry.getValue().getWarning().isEmpty() ? null : TextComponents.translatable("cosmetica.capeServerSettings.warning." + entry.getValue().getWarning().replace(' ', '_')))
		);

		this.addRenderableWidget(list);

		this.addRenderableWidget(new ClassicButton(this.width / 2 - 100, this.height - 25, 200, 20, CommonComponents.GUI_DONE, (button) -> {
			try {
				this.minecraft.setScreen(new UpdatingSettingsScreen(this.parent, this.originalSettings, this.settings));
			} catch (IOException e) {
				e.printStackTrace();
				this.minecraft.setScreen(this.parent);
			} catch (InterruptedException e) {
				e.printStackTrace();
				this.minecraft.setScreen(this.parent);
			}

			try {
				Cosmetica.getConfig().save();
			} catch (IOException e) {
				Cosmetica.LOGGER.warn("Failed to save cosmetica config!");
				e.printStackTrace();
			}
		}));
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		drawCenteredString(matrices, this.font, this.title, this.width / 2, 15, 0xFFFFFF); // re-add title
	}
}
