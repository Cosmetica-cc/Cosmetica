package cc.cosmetica.cosmetica.screens;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.utils.Debug;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class UnauthenticatedScreen extends Screen {
	private Screen parentScreen;
	private boolean fromSave;

	private Component reason = Component.translatable("cosmetica.unauthenticated.message");
	private MultiLineLabel message;
	private int textHeight;

	public UnauthenticatedScreen(Screen parentScreen, boolean fromSave) {
		super(Component.translatable("cosmetica.unauthenticated"));
		this.parentScreen = parentScreen;
		this.fromSave = fromSave;
	}

	@Override
	protected void init() {
		this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
		Objects.requireNonNull(this.font);
		this.textHeight = this.message.getLineCount() * 9;
		int buttonX = this.width / 2 - 100;
		int buttonStartY = Math.min((this.height / 2 + this.textHeight / 2) + 9, this.height - 30);

		if (fromSave) {
			this.addRenderableWidget(new Button(buttonX, buttonStartY, 200, 20, Component.translatable("cosmetica.okay"), button -> this.onClose()));
		} else {
			if (Debug.DEBUG_MODE) { // because I'm not authenticated in dev and can't use the normal button
				this.addRenderableWidget(new Button(buttonX, buttonStartY + 48, 100, 20, TextComponents.literal("Clear Caches"), btn -> {
					Cosmetica.clearAllCaches();
					if (Debug.TEST_MODE) Debug.reloadTestModels();
				}));
			}

			this.addRenderableWidget(new Button(buttonX, buttonStartY, 200, 20, Component.translatable("options.skinCustomisation"), (button) -> {
				this.minecraft.setScreen(new SkinCustomizationScreen(this.parentScreen, Minecraft.getInstance().options));
			}));
			this.addRenderableWidget(new Button(buttonX, buttonStartY + 24, 200, 20, Component.translatable("cosmetica.unauthenticated.retry"), (button) -> {
				minecraft.setScreen(new LoadingScreen(this.parentScreen, minecraft.options));
			}));

			this.addRenderableWidget(new Button(Debug.DEBUG_MODE ? buttonX + 100 : buttonX, buttonStartY + 48, Debug.DEBUG_MODE ? 100 : 200, 20, TextComponents.translatable("gui.cancel"), button -> this.onClose()));
		}
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parentScreen);
	}

	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		Font var10001 = this.font;
		Component var10002 = this.title;
		int var10003 = this.width / 2;
		int var10004 = this.height / 2 - this.textHeight / 2;
		Objects.requireNonNull(this.font);
		drawCenteredString(poseStack, var10001, var10002, var10003, var10004 - 9 * 2, 11184810);
		this.message.renderCentered(poseStack, this.width / 2, this.height / 2 - this.textHeight / 2);
		super.render(poseStack, i, j, f);
	}
}
