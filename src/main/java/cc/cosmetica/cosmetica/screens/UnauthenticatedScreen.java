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

import cc.cosmetica.cosmetica.Authentication;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.utils.DebugMode;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class UnauthenticatedScreen extends Screen {
	private Screen parentScreen;
	private boolean fromSave;

	private final Component reason;
	private MultiLineLabel message;
	private List<TextWithWidth> messageContent;
	private int textHeight;

	public UnauthenticatedScreen(Screen parentScreen, boolean fromSave, UnauthenticatedReason reason) {
		super(new TranslatableComponent(reason.description == UnauthenticatedReason.OFFLINE
				? "cosmetica.offline" : "cosmetica.unauthenticated"));
		this.parentScreen = parentScreen;
		this.fromSave = fromSave;
		this.reason = reason.displayError == null ? reason.description :
				reason.description.copy().append(
						new TextComponent("\n\n\u00A77(" + reason.displayError + ")")
				);
	}

	@Override
	protected void init() {
		this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
		this.messageContent = createMultiLineContent(this.font, this.reason, this.width - 50);

		Objects.requireNonNull(this.font);
		this.textHeight = this.message.getLineCount() * 9;
		int buttonX = this.width / 2 - 100;
		int buttonStartY = Math.min((this.height / 2 + this.textHeight / 2) + 9, this.height - 30);

		if (fromSave) {
			this.addRenderableWidget(new Button(buttonX, buttonStartY, 200, 20, new TranslatableComponent("cosmetica.okay"), button -> this.onClose()));
		} else {
			if (DebugMode.ENABLED) { // because I'm not authenticated in dev and can't use the normal button
				this.addRenderableWidget(new Button(buttonX, buttonStartY + 48, 100, 20, new TextComponent("Clear Caches"), btn -> {
					Cosmetica.clearAllCaches();
					DebugMode.reloadTestModels();
				}));
			}

			this.addRenderableWidget(new Button(buttonX, buttonStartY, 200, 20, new TranslatableComponent("options.skinCustomisation"), (button) -> {
				this.minecraft.setScreen(new SkinCustomizationScreen(this.parentScreen, Minecraft.getInstance().options));
			}));
			this.addRenderableWidget(new Button(buttonX, buttonStartY + 24, 200, 20, new TranslatableComponent("cosmetica.unauthenticated.retry"), (button) -> {
				minecraft.setScreen(new LoadingScreen(this.parentScreen, minecraft.options, Authentication.settingLoadTarget));
			}));
			this.addRenderableWidget(new Button(DebugMode.ENABLED ? buttonX + 100 : buttonX, buttonStartY + 48, DebugMode.ENABLED ? 100 : 200, 20, new TranslatableComponent("gui.cancel"), button -> this.onClose()));
		}
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		// like MultiLineLabel(anonymous)#renderCentered
		int renderX = this.width / 2;
		int renderY = this.height / 2 - this.textHeight / 2;

		for (TextWithWidth textWithWidth : this.messageContent) {
			int lineWidth = font.width(textWithWidth.text);
			int startX = renderX - lineWidth/2;

			// if clicked on that line
			if (y >= renderY && y < renderY + font.lineHeight
					&& x >= startX && x < startX + lineWidth) {
				Style style = font.getSplitter().componentStyleAtWidth(textWithWidth.text, (int) x - startX);

				if (style != null) {
					ClickEvent event = style.getClickEvent();

					if (event != null) {
						if (event.getAction() == ClickEvent.Action.OPEN_URL) {
							MainScreen.copyAndOpenURL(event.getValue());
						}
					}
				}
			}

			renderY += font.lineHeight;
		}

		return super.mouseClicked(x, y, button);
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

	// like MultilineLabel#create
	static List<TextWithWidth> createMultiLineContent(Font font, FormattedText formattedText, int width) {
		return font.split(formattedText, width).stream()
				.map((formattedCharSequence) -> new TextWithWidth(formattedCharSequence, font.width(formattedCharSequence)))
				.collect(ImmutableList.toImmutableList());
	}

	private static class TextWithWidth {
		final FormattedCharSequence text;
		final int width;

		public TextWithWidth(FormattedCharSequence text, int width) {
			this.text = text;
			this.width = width;
		}
	}

	public static class UnauthenticatedReason {
		public UnauthenticatedReason(Component description, @Nullable Exception displayError) {
			this.description = description;
			this.displayError = displayError;
		}

		private final Component description;
		private final Exception displayError;

		private static final MutableComponent STATUS_PAGE = TextComponents.translatable("cosmetica.unauthenticated.here");
		private static final MutableComponent DISCORD_LINK = TextComponents.translatable("cosmetica.unauthenticated.discordSupport");

		static {
			STATUS_PAGE.setStyle(STATUS_PAGE.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://status.cosmetica.cc/")));
			DISCORD_LINK.setStyle(DISCORD_LINK.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://cosmetica.cc/discord")));
		}

		/**
		 * You are offline.
		 */
		public static final Component OFFLINE = TextComponents.translatable("cosmetica.offline.message");

		/**
		 * One of us is offline.
		 */
		public static final Component UNKNOWN_HOST = TextComponents.formattedTranslatable(
				"cosmetica.unauthenticated.unknownHost",
				STATUS_PAGE,
				DISCORD_LINK
		);

		/**
		 * The cosmetica servers are down or having issues.
		 */
		public static final Component CONNECTION_ISSUE = TextComponents.formattedTranslatable(
				"cosmetica.unauthenticated.connectionError",
				DISCORD_LINK
		);

		/**
		 * User is using a cracked minecraft account.
		 */
		public static final Component CRACKED = TextComponents.translatable("cosmetica.unauthenticated.cracked");

		/**
		 * Server sent back a 5xx response
		 */
		public static final Component FIVE_HUNDRED = TextComponents.formattedTranslatable(
				"cosmetica.unauthenticated.serverError",
				DISCORD_LINK
		);

		/**
		 * Generic catch-all for other reasons
		 */
		public static final Component GENERIC = TextComponents.formattedTranslatable(
				"cosmetica.unauthenticated.generic",
				DISCORD_LINK
		);
	}
}
