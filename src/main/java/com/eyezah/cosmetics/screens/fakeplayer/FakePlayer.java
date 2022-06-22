package com.eyezah.cosmetics.screens.fakeplayer;

import com.eyezah.cosmetics.cosmetics.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.PlayerModelPart;

import java.util.UUID;

// Fake player in a normal pose except for the fact that the main arm can be raised.
public class FakePlayer {
	public FakePlayer(Minecraft minecraft, UUID uuid, String name, PlayerData data, boolean slim) {
		var context = new EntityRendererProvider.Context(minecraft.getEntityRenderDispatcher(), minecraft.getItemRenderer(), minecraft.getResourceManager(), minecraft.getEntityModels(), minecraft.font);
		this.model = new PlayerModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim);
		this.data = data;
		this.uuid = uuid;
		this.name = name;
	}

	private final PlayerModel<AbstractClientPlayer> model;
	private final PlayerData data;

	private final UUID uuid;
	private String name;

	private boolean crouching;
	private boolean holdingItem;
	public float xRot;
	public float yRotBody;
	public float yRot;
	public float yRotHead;

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

	public boolean isMainArmRaised() {
		return this.holdingItem;
	}

	public void setMainArmRaised(boolean raised) {
		this.holdingItem = raised;
	}

	public HumanoidArm getMainArm() {
		return HumanoidArm.RIGHT;
	}

	public PlayerModel<AbstractClientPlayer> getModel() {
		return this.model;
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
