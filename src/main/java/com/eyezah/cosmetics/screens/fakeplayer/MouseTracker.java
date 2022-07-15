package com.eyezah.cosmetics.screens.fakeplayer;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class MouseTracker {
	private float lastMouseX;
	private float thisMouseX;

	private float lastMouseY;
	private float thisMouseY;

	private boolean wasDown;
	private boolean isDown;

	private int trackingDataStatePos;
	private int trackingDataStateClick;

	public void update(float mouseX, float mouseY) {
		if (this.trackingDataStatePos < 2) {
			this.trackingDataStatePos++;
		}

		if (this.trackingDataStateClick < 2) {
			this.trackingDataStateClick++;
		}

		this.lastMouseX = this.thisMouseX;
		this.lastMouseY = this.thisMouseY;
		this.wasDown = this.isDown;

		this.thisMouseX = mouseX;
		this.thisMouseY = mouseY;
		this.isDown = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
	}

	public void updatePosition(float mouseX, float mouseY) {
		if (this.trackingDataStatePos < 2) {
			this.trackingDataStatePos++;
		}

		this.lastMouseX = this.thisMouseX;
		this.lastMouseY = this.thisMouseY;

		this.thisMouseX = mouseX;
		this.thisMouseY = mouseY;
	}

	public void updateClick(boolean mouseDown) {
		if (this.trackingDataStateClick < 2) {
			this.trackingDataStateClick++;
		}

		this.wasDown = this.isDown;
		this.isDown = mouseDown;
	}

	public void setMouseDown(boolean mouseDown) {
		this.isDown = mouseDown;
	}

	public void pushMouseDown() {
		this.wasDown = this.isDown;
	}

	public boolean hasTrackingPosData() {
		return this.trackingDataStatePos == 2;
	}

	public boolean hasTrackingClickData() {
		return this.trackingDataStateClick == 2;
	}

	public boolean isMouseDown() {
		return this.isDown;
	}

	public boolean wasMouseDown() {
		return this.wasDown;
	}

	public boolean wasMousePressed() {
		return this.isDown && !this.wasDown;
	}

	public boolean wasMouseReleased() {
		return !this.isDown && this.wasDown;
	}

	public float deltaMouseX() {
		return this.thisMouseX - this.lastMouseX;
	}

	public float deltaMouseY() {
		return this.thisMouseY - this.lastMouseY;
	}
}
