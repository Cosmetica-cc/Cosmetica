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

import benzenestudios.sulphate.Anchor;
import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.CosmeticPosition;
import cc.cosmetica.api.ServerResponse;
import cc.cosmetica.api.User;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.screens.widget.TextWidget;
import cc.cosmetica.cosmetica.utils.TextComponents;
import cc.cosmetica.util.SafeURL;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StealHisLookScreen extends SulphateScreen {
	public StealHisLookScreen(Component pronounifiedTitle, PlayerData look, PlayerData ownLook, SnipeScreen parentScreen) {
		super(pronounifiedTitle, parentScreen);
		this.confirm = TextComponents.formattedTranslatable("cosmetica.stealhislook.confirm", "their");
		this.look = look;
		this.currentLook = ownLook;
		this.ownProfile = parentScreen.ownProfile;

		this.setRows(2);
		this.setAnchorY(Anchor.TOP, () -> this.height / 2 + 24);
	}

	private final Component confirm;
	private final PlayerData currentLook;
	private final PlayerData look;
	private final User ownProfile;

	@Override
	protected void addWidgets() {
		this.addRenderableWidget(new TextWidget(this.width / 2 - 100, this.height / 2, this.minecraft.font.width(this.confirm), 20, true, this.confirm));
		this.addButton(CommonComponents.GUI_YES, b -> this.minecraft.setScreen(new UpdatingCosmeticsScreen<>(
				new CustomiseCosmeticsScreen(null, new FakePlayer(this.minecraft, this.ownProfile.getUUID(), this.ownProfile.getUsername(), this.currentLook), ((SnipeScreen)this.parent).settings),
				() -> {
					List<CompletableFuture<?>> requests = new ArrayList<>(6);

					// minimise requests by not duplicating
					// don't set for 3p capes
					if (!this.look.cape().isThirdPartyCape() && !removeNullLikeAChad(this.look.cape().getActualImage()).equals(removeNullLikeAChad(this.currentLook.cape().getActualImage()))) {
						requests.add(CompletableFuture.supplyAsync(() -> setCosmeticNotifyErr(CosmeticPosition.CAPE, this.look.cape().getId())));
					}

					if (!id(this.look.backBling()).equals(id(this.currentLook.backBling()))) {
						requests.add(CompletableFuture.supplyAsync(() -> setCosmeticNotifyErr(CosmeticPosition.BACK_BLING, this.look.backBling() == null ? "none" : this.look.backBling().id())));
					}

					if (!id(this.look.leftShoulderBuddy()).equals(id(this.currentLook.leftShoulderBuddy()))) {
						requests.add(CompletableFuture.supplyAsync(() -> setCosmeticNotifyErr(CosmeticPosition.LEFT_SHOULDER_BUDDY, this.look.leftShoulderBuddy() == null ? "none" : this.look.leftShoulderBuddy().id())));
					}

					if (!id(this.look.rightShoulderBuddy()).equals(id(this.currentLook.rightShoulderBuddy())) ){
						requests.add(CompletableFuture.supplyAsync(() -> setCosmeticNotifyErr(CosmeticPosition.RIGHT_SHOULDER_BUDDY, this.look.rightShoulderBuddy() == null ? "none": this.look.rightShoulderBuddy().id())));
					}

					// the hat extravaganza
					// this could probably be cleaned up a lot

					if (this.look.hats().isEmpty()) {
						if (!this.currentLook.hats().isEmpty()) {
							// the look has hats but you don't have any

							if (this.currentLook.hats().size() > 1) {
								requests.add(CompletableFuture.supplyAsync(() -> setCosmeticNotifyErr(CosmeticPosition.SECOND_HAT, "none")).thenCompose(jj -> CompletableFuture.supplyAsync(() -> setCosmeticNotifyErr(CosmeticPosition.HAT, "none"))));
							}
							else {
								requests.add(CompletableFuture.supplyAsync(() -> setCosmeticNotifyErr(CosmeticPosition.HAT, "none")));
							}
						}
					}
					else if (this.look.hats().size() > 1) {
						// the look has 2 hats, you may have 0-2 hats

						if (this.currentLook.hats().isEmpty()) {
							// you need to add both hats
							requests.add(CompletableFuture.supplyAsync(() -> setCosmeticNotifyErr(CosmeticPosition.HAT, this.look.hats().get(0).id())).thenCompose(jj -> CompletableFuture.supplyAsync(() -> setCosmeticNotifyErr(CosmeticPosition.SECOND_HAT, this.look.hats().get(1).id()))));
						}
						else {
							// if the first hat differs, match
							if (id(this.currentLook.hats().get(0)) != id(this.look.hats().get(0))) {
								requests.add(CompletableFuture.supplyAsync(() -> setCosmeticNotifyErr(CosmeticPosition.HAT, this.look.hats().get(0).id())));
							}

							// if no second hat or second hat differs, match
							if (this.currentLook.hats().size() < 2 || id(this.currentLook.hats().get(1)) != id(this.look.hats().get(1))) { // different hat2
								requests.add(CompletableFuture.supplyAsync(() -> setCosmeticNotifyErr(CosmeticPosition.SECOND_HAT, this.look.hats().get(1).id())));
							}
						}
					}
					else {
						// the look has 1 hat, you may have 0-2 hats

						// if you have no hats or your first hat differs, match
						if (this.currentLook.hats().isEmpty() || id(this.currentLook.hats().get(0)) != id(this.look.hats().get(0))) { // different hat
							requests.add(CompletableFuture.supplyAsync(() -> setCosmeticNotifyErr(CosmeticPosition.HAT, this.look.hats().get(0).id())));
						}

						if (this.currentLook.hats().size() > 1) {
							// remove second hat
							requests.add(CompletableFuture.supplyAsync(() -> setCosmeticNotifyErr(CosmeticPosition.SECOND_HAT, "none")));
						}
					}

					CompletableFuture.allOf(requests.toArray(new CompletableFuture[0])).join();
					return new ServerResponse<>(new Object(), SafeURL.direct("https://example.com")); // dummy. it ignores it.
				}
		)));
		this.addButton(CommonComponents.GUI_NO, b -> this.onClose());
	}

	public static ResourceLocation removeNullLikeAChad(@Nullable ResourceLocation rl) {
		return rl == null ? MissingTextureAtlasSprite.getLocation() : rl;
	}

	private static ServerResponse<Boolean> setCosmeticNotifyErr(CosmeticPosition position, String id) {
		ServerResponse<Boolean> response = Cosmetica.api.setCosmetic(position, id);
		response.ifError(Cosmetica.logErr("Error setting " + position.getUrlString() + " in steal-his-look"));
		return response;
	}
	
	private static String id(@Nullable BakableModel model) {
		return model == null ? "none" : model.id();
	}
}
