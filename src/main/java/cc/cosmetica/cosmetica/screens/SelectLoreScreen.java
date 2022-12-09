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
import benzenestudios.sulphate.ExtendedScreen;
import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.LoreType;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.screens.widget.FetchingCosmetics;
import cc.cosmetica.cosmetica.screens.widget.StringSelection;
import cc.cosmetica.cosmetica.screens.widget.TextWidget;
import cc.cosmetica.cosmetica.utils.TextComponents;
import cc.cosmetica.impl.CosmeticaWebAPI;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SelectLoreScreen extends SulphateScreen {
	protected SelectLoreScreen(@Nullable Screen parent, String lore) {
		super(TextComponents.translatable("cosmetica.selection.select").append(TextComponents.translatable("cosmetica.entry.Lore")), parent);
		this.baseTitle = TextComponents.translatable("cosmetica.entry.Lore");
		if (!lore.isEmpty() && lore.charAt(0) == '\u00A7') this.colour = lore.substring(0, 2);
		this.lore = this.colour.isEmpty() ? lore : lore.substring(2);
		this.originalLore = this.lore;
		this.showing = LoreType.TITLES;
	}

	private List<String> titles = ImmutableList.of();
	private List<String> pronouns = ImmutableList.of();
	private boolean auth;
	private FetchingCosmetics fetching;
	private StringSelection list;
	private final MutableComponent baseTitle;
	private String lore;
	private String colour = "";
	private final String originalLore;
	private LoreType showing; // uses TWITCH for all socials, for now
	private boolean setPronouns;
	private int pronounsSelected;

	// for message
	@Nullable MultiLineLabel pleaseUseWebsite;

	@Override
	protected void addWidgets() {
		if (this.fetching == null) {
			this.fetching = this.addWidget((x, y, w, h, component) -> new FetchingCosmetics<>("Lore", () -> ImmutableList.of(Cosmetica.api.getLoreList(LoreType.TITLES), Cosmetica.api.getLoreList(LoreType.PRONOUNS)), (obj, titles) -> {
				if (obj == this.fetching) {
					if (titles.isEmpty()) {
						this.auth = false;
					}
					else {
						this.titles = titles.get(0);
						this.pronouns = titles.get(1);
						this.auth = true;
					}

					this.minecraft.tell(() -> {if (this.minecraft.screen == this) this.init(this.minecraft, this.width, this.height);});
				}
			}), TextComponents.dummy());

			this.addButton(CommonComponents.GUI_CANCEL, b -> this.onClose());
		}
		else if (this.auth) {
			if (this.showing == LoreType.TWITCH) {
				this.pleaseUseWebsite = MultiLineLabel.create(this.font, TextComponents.translatable("cosmetica.selection.lore.pleaseusethewebsite"), this.width - 50);

				this.addRenderableWidget(new ClassicButton(this.width / 2 - 100, this.height / 2 + this.pleaseUseWebsite.getLineCount() * 10 + 4, 200, 20, TextComponents.translatable("cosmetica.openWebPanel"), button -> {
					try {
						Minecraft.getInstance().keyboardHandler.setClipboard(Cosmetica.websiteHost + "/manage?" + ((CosmeticaWebAPI)Cosmetica.api).getMasterToken());
						Util.getPlatform().openUri(Cosmetica.websiteHost + "/manage?" + ((CosmeticaWebAPI)Cosmetica.api).getMasterToken());
					} catch (Exception e) {
						throw new RuntimeException("bruh", e); // this is too funny to change to a more serious message I'm sorry
					}
				}));
			}
			else {
				this.pleaseUseWebsite = null; // don't show this

				// create the selection which renders the text and handles clicking on options
				StringSelection list = new StringSelection(this.minecraft, this, this.font, s -> {
					// it runs set with "" on opening
					if (!"".equals(s)) {
						switch (this.showing) {
						case PRONOUNS:
							if (this.pronounsSelected == 0) {
								this.setPronouns = true;
								this.lore = s;
								this.pronounsSelected = 1;
							} else if (this.pronounsSelected < 4) {
								this.setPronouns = true;
								this.lore += "/" + s;
								this.pronounsSelected++;
							}
							break;
						case TITLES:
							this.setPronouns = false;
							this.lore = s;
							break;
						}

						this.updateTitle();
					}
				});

				// add the available options to select to the lists
				switch (this.showing) {
				case PRONOUNS:
					this.pronounsSelected = 0;
					this.pronouns.forEach(list::addItem);
					break;
				case TITLES:
					this.titles.forEach(list::addItem);
					list.selectItem(this.lore);
					list.recenter();
					break;
				}

				// if we already have a list match the selected one if it's present in both
				if (this.list != null) {
					list.matchSelected(this.list);
				}

				this.list = list; // update
				this.addRenderableWidget(this.list);
			}

			this.updateTitle();

			this.addRenderableWidget(new ClassicButton(this.width / 2 - 102, this.height - 52, 100, 20, TextComponents.translatable(getTranslationKey(cycle(this.showing))), b -> {
				this.showing = cycle(this.showing);
				this.list = null;
				this.init(this.minecraft, this.width, this.height);
			}));

			this.addRenderableWidget(new ClassicButton(this.width / 2 + 2, this.height - 52, 100, 20, TextComponents.translatable("cosmetica.selection.lore.clear"), b -> {
				this.lore = "";
				this.pronounsSelected = 0;
				this.updateTitle();
			}));

			this.addRenderableWidget(new ClassicButton(this.width / 2 - 102, this.height - 28, 100, 20, CommonComponents.GUI_CANCEL, b -> this.onClose()));

			this.addRenderableWidget(new ClassicButton(this.width / 2 + 2, this.height - 28, 100, 20, CommonComponents.GUI_DONE, b -> {
				if (this.lore.equals(this.originalLore)) {
					this.onClose();
				}
				else {
					this.minecraft.setScreen(new UpdatingCosmeticsScreen<>(this.parent, () -> Cosmetica.api.setLore(this.lore.isEmpty() ? LoreType.NONE : (this.setPronouns ? LoreType.PRONOUNS : LoreType.TITLES), this.lore)));
				}
			}));
		}
		else {
			this.addWidget((x, y, w, h, component) -> new TextWidget(x, y, w, h, true, component), TextComponents.translatable("cosmetica.selection.lore.err"));
			this.addButton(TextComponents.translatable("cosmetica.okay"), b -> this.onClose());
		}
	}

	private void updateTitle() {
		((ExtendedScreen) this).setTitle(this.lore.isEmpty() ? this.baseTitle : this.baseTitle.copy().append(TextComponents.literal(": " + this.colour + this.lore)));
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		if (this.pleaseUseWebsite == null) {
			// re-add title because it's drawn over with a selection list present (which is used when there is no please use website notice)
			drawCenteredString(matrices, this.font, this.title, this.width / 2, 15, 0xFFFFFF);
		}
		else {
			this.pleaseUseWebsite.renderCentered(matrices, this.width / 2, this.height / 2);
		}
	}

	private static String getTranslationKey(LoreType type) {
		return switch (type) {
			case TITLES -> "cosmetica.selection.lore.titles";
			case PRONOUNS -> "cosmetica.selection.lore.pronouns";
			case TWITCH -> "cosmetica.selection.lore.twid";
			default -> "cosmetica.selection.lore.missingno";
		};
	}

	private static LoreType cycle(LoreType current) {
		return switch (current) {
			case TITLES -> LoreType.PRONOUNS;
			case PRONOUNS -> LoreType.TWITCH;
			default -> LoreType.TITLES;
		};
	}
}
