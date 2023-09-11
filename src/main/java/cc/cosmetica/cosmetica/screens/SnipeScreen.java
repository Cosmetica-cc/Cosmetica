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

import benzenestudios.sulphate.ClassicButton;
import cc.cosmetica.api.User;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.utils.TextComponents;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SnipeScreen extends ViewCosmeticsScreen {
	public SnipeScreen(Component title, Screen parentScreen, FakePlayer player, ServerOptions options,
					   PlayerData ownData, User ownProfile) {
		super(title, parentScreen, player, options);
		this.stealTheirLook = TextComponents.formattedTranslatable("cosmetica.stealhislook.steal", "their");
		this.ownData = ownData;
		this.options = options;
		this.ownProfile = ownProfile;
	}

	private Component stealTheirLook;
	private final PlayerData ownData;
	final ServerOptions options;
	final User ownProfile;

	// funny hack to add both
	@Override
	protected AbstractButton addDone(int y) {
		this.addRenderableWidget(new ClassicButton(this.width / 2 - 100, y - 24, 200, 20, this.stealTheirLook, b -> this.minecraft.setScreen(new StealHisLookScreen(this.stealTheirLook, this.fakePlayer.getData(), this.ownData, this))));
		return super.addDone(y);
	}
}
