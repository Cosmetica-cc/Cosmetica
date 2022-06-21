package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.SulphateScreen;
import com.eyezah.cosmetics.utils.TextComponents;
import net.minecraft.client.gui.screens.Screen;

public class CustomiseCosmeticsScreen extends SulphateScreen {
	protected CustomiseCosmeticsScreen(Screen parentScreen) {
		super(TextComponents.translatable("cosmetica.customizeCosmetics"), parentScreen);
	}

	@Override
	protected void addWidgets() {
		this.addDone();
	}
}
