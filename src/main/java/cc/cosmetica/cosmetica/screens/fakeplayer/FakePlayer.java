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

package cc.cosmetica.cosmetica.screens.fakeplayer;

import cc.cosmetica.cosmetica.cosmetics.Hats;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.cosmetics.ShoulderBuddies;
import cc.cosmetica.cosmetica.utils.TextComponents;
import cc.cosmetica.cosmetica.cosmetics.BackBling;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

// Fake player in a normal pose except for the fact that the main arm can be raised.
public class FakePlayer implements RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>, Playerish {
	public FakePlayer(Minecraft minecraft, UUID uuid, String name, PlayerData data) {
		this.data = data;
		this.uuid = uuid;
		this.name = name;

		// initialise layers
		this.layers.add(new MenuCapeLayer());
		this.layers.add(new Hats<>(this));
		this.layers.add(new ShoulderBuddies<>(this, Minecraft.getInstance().getEntityModels()));
		this.layers.add(new BackBling<>(this));

		// initialise model
		this.verifyModel(minecraft);
	}

	@Nullable private PlayerModel<AbstractClientPlayer> model;
	private PlayerData data;
	private final List<MenuRenderLayer> layers = new LinkedList<>();

	private final UUID uuid;
	private String name;

	public boolean renderNametag = true;

	private boolean crouching;
	private boolean holdingItem;
	public float xRot;
	public float yRotBody;
	public float yRot;
	public float yRotHead;
	public int tickCount = 0;

	/**
	 * Verify this fake player has a loaded model. Will try load one if it doesn't.
	 * @param minecraft minecraft.
	 * @return whether this fake player has a model.
	 */
	public boolean verifyModel(Minecraft minecraft) {
		if (this.model == null) {
			try {
				var context = new EntityRendererProvider.Context(
						minecraft.getEntityRenderDispatcher(),
						minecraft.getItemRenderer(),
						minecraft.getResourceManager(),
						minecraft.getEntityModels(),
						minecraft.font);

				this.model = new PlayerModel<>(context.bakeLayer(this.data.slim() ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), this.data.slim());
			} catch (IllegalArgumentException e) {
				if (!e.getMessage().toUpperCase(Locale.ROOT).contains("NO MODEL FOR LAYER")) {
					e.printStackTrace();
				}
				return false;
			}
		}

		return true;
	}

	@Override
	public Vec3 getVelocity() {
		return Vec3.ZERO;
	}

	public boolean isModelPartShown(PlayerModelPart part) {
		return true;
	}

	@Override
	public boolean isSneaking() {
		return this.crouching;
	}

	@Override
	public boolean renderDiscreteNametag() {
		return this.crouching;
	}

	@Override
	public int getLifetime() {
		return this.tickCount;
	}

	@Override
	public int getPseudoId() {
		return (int) this.uuid.getMostSignificantBits();
	}

	public void setCrouching(boolean crouching) {
		this.crouching = crouching;
	}

	public PlayerData getData() {
		return this.data;
	}

	@Override
	public PlayerData getCosmeticaPlayerData() {
		return this.data;
	}

	public void setData(PlayerData data) {
		this.data = data;
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public String getName() {
		return this.name;
	}

	public Component getDisplayName() {
		return TextComponents.literal((this.data.icon() == null ? "" : "\u2001") + this.data.prefix() + this.name + this.data.suffix());
	}

	public ResourceLocation getSkin() {
		return Minecraft.getInstance().getTextureManager().getTexture(this.data.skin(), MissingTextureAtlasSprite.getTexture()) == MissingTextureAtlasSprite.getTexture() ?
				DefaultPlayerSkin.getDefaultSkin(this.uuid) : this.data.skin();
	}

	public ResourceLocation getRenderableCape() {
		if (this.data.cape().getImage() == null) return MissingTextureAtlasSprite.getLocation();
		return Minecraft.getInstance().getTextureManager().getTexture(this.data.cape().getImage(), MissingTextureAtlasSprite.getTexture()) == MissingTextureAtlasSprite.getTexture() ?
				MissingTextureAtlasSprite.getLocation() : this.data.cape().getImage();
	}

	public Iterable<MenuRenderLayer> getLayers() {
		return this.layers;
	}

	public boolean isMainArmRaised() {
		return this.holdingItem;
	}

	public void setMainArmRaised(boolean raised) {
		this.holdingItem = raised;
	}

	public HumanoidArm getMainArm() {
		return HumanoidArm.RIGHT;
	}

	@Override
	public @org.jetbrains.annotations.Nullable PlayerModel<AbstractClientPlayer> getModel() {
		return this.model;
	}

	@Override
	public ResourceLocation getTextureLocation(@Nullable AbstractClientPlayer entity) {
		return this.getSkin();
	}

	public float getYRotBody(float delta) {
		return yRotBody;
	}

	public float getYRot(float delta) {
		return yRot;
	} // ???

	public float getYRotHead(float delta) {
		return yRotHead;
	}

	public float getXRot(float delta) {
		return xRot;
	}

	public Pose getPose() {
		return this.crouching ? Pose.CROUCHING : Pose.STANDING;
	}
}
