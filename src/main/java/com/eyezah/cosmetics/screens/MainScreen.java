package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.Anchor;
import benzenestudios.sulphate.SulphateScreen;
import com.eyezah.cosmetics.Authentication;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.utils.Debug;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.IOException;

public class MainScreen extends SulphateScreen {
	public MainScreen(Screen parentScreen, boolean doShoulderBuddies, boolean doHats, boolean doRegionSpecificEffects, boolean doLore) {
		super(Component.translatable("cosmetica.cosmeticsMenu"), parentScreen);
		this.parentScreen = parentScreen;

		this.oldOptions = new ServerOptions(doShoulderBuddies, doHats, doRegionSpecificEffects, doLore);
		this.newOptions = new ServerOptions(this.oldOptions);

		this.setAnchorY(Anchor.TOP, () -> this.height / 6 + 12);
		this.setRows(2);
	}

	private final Screen parentScreen;
	private final ServerOptions oldOptions;
	private final ServerOptions newOptions;

	private boolean doReload;
	private boolean doTestReload;


	private Component generateButtonToggleText(String translatable, boolean toggle) {
		MutableComponent component = Component.literal("");
		component.append(Component.translatable(translatable));
		component.append(": ");
		if (toggle) {
			component.append(Component.translatable("cosmetica.enabled"));
		} else {
			component.append(Component.translatable("cosmetica.disabled"));
		}
		return component;
	}

	@Override
	protected void addWidgets() {
		// top row

		this.addButton(Component.translatable("options.skinCustomisation"), (button) -> {
			this.minecraft.setScreen(new SkinCustomizationScreen(this, Minecraft.getInstance().options));
		});

		this.addButton(generateButtonToggleText("cosmetica.australians", this.newOptions.regionSpecificEffects.get()), button -> {
			this.newOptions.regionSpecificEffects.toggle();
			button.setMessage(generateButtonToggleText("cosmetica.australians", this.newOptions.regionSpecificEffects.get()));
		});

		// second row, etc...

		this.addButton(Component.translatable("cosmetica.reloadCosmetics"), (button) -> {
			doReload = !doReload;
			if (Debug.TEST_MODE) doTestReload = doReload;
			if (doReload) {
				button.setMessage(Component.translatable("cosmetica.willReload"));
			} else {
				button.setMessage(Component.translatable("cosmetica.reloadCosmetics"));
			}
		});

		this.addButton(generateButtonToggleText("cosmetica.doHats", this.newOptions.hats.get()), button -> {
			this.newOptions.hats.toggle();
			button.setMessage(generateButtonToggleText("cosmetica.doHats", this.newOptions.hats.get()));
		});

		//

		this.addButton(generateButtonToggleText("cosmetica.showNametagInThirdPerson", Cosmetica.getConfig().shouldShowNametagInThirdPerson()), (button) -> {
			Cosmetica.getConfig().setShowNametagInThirdPerson(!Cosmetica.getConfig().shouldShowNametagInThirdPerson());
			button.setMessage(generateButtonToggleText("cosmetica.showNametagInThirdPerson", Cosmetica.getConfig().shouldShowNametagInThirdPerson()));
		});

		this.addButton(generateButtonToggleText("cosmetica.doShoulderBuddies", this.newOptions.shoulderBuddies.get()), (button) -> {
			this.newOptions.shoulderBuddies.toggle();
			button.setMessage(generateButtonToggleText("cosmetica.doShoulderBuddies", this.newOptions.shoulderBuddies.get()));
		});

		this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 - 12 + 24 * 4, 150, 20, generateButtonToggleText("cosmetica.doLore", this.newOptions.lore.get()), (button) -> {
			this.newOptions.lore.toggle();
			button.setMessage(generateButtonToggleText("cosmetica.doLore", this.newOptions.lore.get()));
		}));

		/*this.addButton(generateButtonToggleText("cosmetica.doLore", this.newOptions.lore.get()), (button) -> {
			this.newOptions.lore.toggle();
			button.setMessage(generateButtonToggleText("cosmetica.doLore", this.newOptions.lore.get()));
		});*/ // MOVE TO RIGHT (dont have the time to see how sulphate lol)

		/*if (Debug.TEST_MODE) {
			this.addButton(200, 20, Component.translatable("cosmetica.reloadTestCosmetics"), (button) -> {
				doTestReload = !doTestReload;

				if (doTestReload) {
					button.setMessage(Component.translatable("cosmetica.willReload"));
				} else {
					button.setMessage(Component.translatable("cosmetica.reloadTestCosmetics"));
				}
			});
		}*/

		// bottom of the menu
		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 - 12 + 24 * 6, 200, 20, Component.translatable("cosmetica.customizeCosmetics"), (button) -> {
			try {
				Minecraft.getInstance().keyboardHandler.setClipboard(Cosmetica.websiteHost + "/manage?" + Authentication.getToken());
				Util.getPlatform().openUri(Cosmetica.websiteHost + "/manage?" + Authentication.getToken());
			} catch (Exception e) {
				throw new RuntimeException("bruh", e);
			}
		}));

		// when done, update settings
		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 - 12 + 24 * 7, 200, 20, CommonComponents.GUI_DONE, (button) -> {
			if (this.doTestReload) {
				Debug.loadTestProperties();
				Debug.loadTestModel(Debug.LocalModelType.HAT);
				Debug.loadTestModel(Debug.LocalModelType.SHOULDERBUDDY);
			}

			try {
				this.minecraft.setScreen(new UpdatingSettingsScreen(this.parentScreen, this.oldOptions, this.newOptions, this.doReload));
			} catch (IOException e) {
				e.printStackTrace();
				this.minecraft.setScreen(this.parentScreen);
			} catch (InterruptedException e) {
				e.printStackTrace();
				this.minecraft.setScreen(this.parentScreen);
			}

			try {
				Cosmetica.getConfig().save();
			} catch (IOException e) {
				Cosmetica.LOGGER.warn("Failed to save cosmetica config!");
				e.printStackTrace();
			}
		}));
	}

	// on close is like cancel
	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parentScreen);
	}
}
