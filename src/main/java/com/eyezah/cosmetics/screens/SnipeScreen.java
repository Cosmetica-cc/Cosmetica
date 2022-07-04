package com.eyezah.cosmetics.screens;

import cc.cosmetica.api.UserSettings;
import com.eyezah.cosmetics.cosmetics.PlayerData;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayer;
import com.eyezah.cosmetics.utils.TextComponents;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SnipeScreen extends ViewCosmeticsScreen {
	public SnipeScreen(Component title, Screen parentScreen, FakePlayer player, UserSettings settings, PlayerData ownData) {
		super(title, parentScreen, player, settings);
		this.stealTheirLook = TextComponents.formattedTranslatable("cosmetica.stealhislook.steal", "their");
		this.ownData = ownData;
	}

	private Component stealTheirLook;
	private final PlayerData ownData;

	// funny hack to add both
	@Override
	protected AbstractButton addDone(int y) {
		this.addRenderableWidget(new Button(this.width / 2 - 100, y - 24, 200, 20, this.stealTheirLook, b -> this.minecraft.setScreen(new StealHisLookScreen(this.stealTheirLook, this.fakePlayer.getData(), this.ownData, this))));
		return super.addDone(y);
	}
}
