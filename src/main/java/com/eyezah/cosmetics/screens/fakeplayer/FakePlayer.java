package com.eyezah.cosmetics.screens.fakeplayer;

import com.eyezah.cosmetics.cosmetics.BackBling;
import com.eyezah.cosmetics.cosmetics.Hats;
import com.eyezah.cosmetics.cosmetics.PlayerData;
import com.eyezah.cosmetics.cosmetics.ShoulderBuddies;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.TextComponents;
import net.minecraft.client.Minecraft;
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

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

// Fake player in a normal pose except for the fact that the main arm can be raised.
public class FakePlayer implements RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public FakePlayer(Minecraft minecraft, UUID uuid, String name, PlayerData data, boolean slim) {
		var context = new EntityRendererProvider.Context(minecraft.getEntityRenderDispatcher(), minecraft.getItemRenderer(), minecraft.getResourceManager(), minecraft.getEntityModels(), minecraft.font);
		this.model = new PlayerModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim);

		this.data = data;
		this.uuid = uuid;
		this.name = name;

		// initialise layers
		this.layers.add(new MenuCapeLayer());
		this.layers.add(new Hats<>(this));
		this.layers.add(new ShoulderBuddies<>(this, Minecraft.getInstance().getEntityModels()));
		this.layers.add(new BackBling<>(this));
	}

	private final PlayerModel<AbstractClientPlayer> model;
	private static PlayerModel<AbstractClientPlayer> models;
	private final PlayerData data;
	private final List<MenuRenderLayer> layers = new LinkedList<>();

	private final UUID uuid;
	private String name;

	private boolean crouching;
	private boolean holdingItem;
	public float xRot;
	public float yRotBody;
	public float yRot;
	public float yRotHead;
	public int tickCount = 0;

	public boolean isModelPartShown(PlayerModelPart part) {
		return true;
	}

	public boolean isCrouching() {
		return this.crouching;
	}

	public void setCrouching(boolean crouching) {
		this.crouching = crouching;
	}

	public PlayerData getData() {
		return this.data;
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public String getName() {
		return this.name;
	}

	public Component getDisplayName() {
		return TextComponents.literal(this.name);
	}

	public ResourceLocation getSkin() {
		return Minecraft.getInstance().getTextureManager().getTexture(this.data.skin(), MissingTextureAtlasSprite.getTexture()) == MissingTextureAtlasSprite.getTexture() ?
				DefaultPlayerSkin.getDefaultSkin(this.uuid) : this.data.skin();
	}

	public ResourceLocation getCape() {
		return Minecraft.getInstance().getTextureManager().getTexture(this.data.cape(), MissingTextureAtlasSprite.getTexture()) == MissingTextureAtlasSprite.getTexture() ?
				MissingTextureAtlasSprite.getLocation() : this.data.cape();
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
	public PlayerModel<AbstractClientPlayer> getModel() {
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
