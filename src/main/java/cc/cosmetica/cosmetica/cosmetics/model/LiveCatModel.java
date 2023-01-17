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

package cc.cosmetica.cosmetica.cosmetics.model;

import cc.cosmetica.cosmetica.screens.fakeplayer.Playerish;
import cc.cosmetica.cosmetica.utils.MutableOptionalFloat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cat;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LiveCatModel extends CatModel<Cat> {
	public LiveCatModel() {
		super(0);

		this.backLegL.xRot = (float)Math.PI / 6f;
		this.backLegR.xRot = (float)Math.PI / 6f;
	}

	void pose(Playerish player) {
		// general stuff
		this.frontLegL.xRot = (float)-Math.PI / 3f;
		this.frontLegL.y = 14.1f + 2f;
		this.frontLegL.z = -5.0f + 1f;

		this.frontLegR.xRot = (float)-Math.PI / 3f;
		this.frontLegR.y = 14.1f + 2f;
		this.frontLegR.z = -5.0f + 1f;

		this.backLegL.z = 5.0f + 0.2f;
		this.backLegR.z = 5.0f + 0.2f;

		// falling leg raise
		float hindLegRot = (float)Math.PI / 6f + (player.getVelocity().y < 0 ? Mth.clamp((float) -player.getVelocity().y, 0, (float)Math.PI / 4f): 0);
		final float minShift = 0.005f;

		if (Math.abs(hindLegRot - this.backLegL.xRot) < 0.03f) {
			this.backLegL.xRot = hindLegRot;
			this.backLegR.xRot = hindLegRot;
		}
		else {
			float hindLegRotMod = (hindLegRot - this.backLegL.xRot) * 0.1f;
			if (Math.abs(hindLegRotMod) < minShift) hindLegRotMod = minShift;

			this.backLegL.xRot += hindLegRotMod;
			this.backLegR.xRot += hindLegRotMod;
		}

		// animations master timer
		// 51.2 second cycle
		final int cycle = player.getLifetime() % 1024;

		// tail animation
		float tailRotation = MutableOptionalFloat.empty()
				.computeIfAbsent(() -> TAIL_SWING.getNullable(50, cycle)) // 2.5s
				.computeIfAbsent(() -> TAIL_SWING.getNullable(550, cycle)) // 27.5s
				.orElse(0);

		this.tail1.zRot = tailRotation;
		this.tail2.zRot = this.tail1.zRot;

		this.tail2.y = 20.0f + 4.19f * Mth.cos(this.tail1.zRot) - 5.04f;
		this.tail2.x = -4.19f * Mth.sin(this.tail1.zRot);
		this.tail2.z = 14.0f - 0.1f;

		// licking animation

		float headRot = MutableOptionalFloat.empty()
				.computeIfAbsent(() -> HEAD_ROT.getNullable(0, cycle)) // 2.5s
				.computeIfAbsent(() -> HEAD_ROT_LONG.getNullable(820, cycle)) // 41s
				.orElse(0);

		this.head.yRot = headRot;

		float pawRaise = 0;

		int cycle_m5 = (cycle - 5) % 1024;

		if (HEAD_ROT.getNullable(0, cycle) != null || HEAD_ROT_LONG.getNullable(820, cycle) != null) {
			pawRaise = 1;
		}
		else {
			// transition
			pawRaise = MutableOptionalFloat.empty()
					.computeIfAbsent(() -> RAISE_ARM.getNullable(1024 - RAISE_ARM.duration, cycle_m5))
					.computeIfAbsent(() -> RAISE_ARM.getNullable(820 - RAISE_ARM.duration + 5, cycle))
					.computeIfAbsent(() -> LOWER_ARM.getNullable(0 + HEAD_ROT.duration, cycle))
					.computeIfAbsent(() -> LOWER_ARM.getNullable(820 + HEAD_ROT_LONG.duration, cycle))
					.orElse(0);
		}


		this.frontLegR.yRot = pawRaise * 0.2f;
		this.frontLegR.xRot -= pawRaise * 0.62f; // adjusting from base position established at beginning of method
	}

	public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
		this.body.xRot += 0.5 * Math.PI; // stolen cursed hack from the sheep
		this.renderToBuffer(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
		this.body.xRot -= 0.5 * Math.PI;
	}

	private static class Animation {
		public Animation(float startValue) {
			this.startValue = startValue;
		}

		private final float startValue;
		private final List<Spline> spline = new LinkedList<>();
		private int duration;

		public Animation then(int duration, float endValue, FloatUnaryOperator ease) {
			float startValue = this.startValue;
			int startTicks = 0;

			if (!this.spline.isEmpty()) {
				Spline last = this.spline.get(this.spline.size() - 1);
				startValue = last.endValue;
				startTicks = last.endTicks;
			}

			this.spline.add(new Spline(startTicks, startTicks + duration, startValue, endValue, ease));
			this.duration += duration;
			return this;
		}

		public Animation repeat(int nTimes) {
			List<Spline> group = new ArrayList<>(this.spline);

			for (int i = 0; i < nTimes; i++) {
				for (Spline spline : group) {
					this.then(spline.endTicks - spline.startTicks, spline.endValue, spline.ease);
				}
			}

			return this;
		}

		@Nullable
		public Float getNullable(int startTicks, int currentTicks) {
			if (this.shouldRun(startTicks, currentTicks)) {
				return this.get(startTicks, currentTicks);
			}

			return null;
		}

		public float get(int startTicks, int currentTicks) {
			currentTicks -= startTicks;

			for (Spline spline : this.spline) {
				if (currentTicks < spline.endTicks) {
					return spline.get(currentTicks);
				}
			}

			return 0;
		}

		public boolean shouldRun(int startTicks, int currentTicks) {
			currentTicks -= startTicks;
			return currentTicks >= 0 && currentTicks < this.duration;
		}
	}

	private static class Spline {
		Spline(int startTicks, int endTicks, float startValue, float endValue, FloatUnaryOperator ease) {
			this.startTicks = startTicks;
			this.endTicks = endTicks;
			this.startValue = startValue;
			this.endValue = endValue;
			this.ease = ease;
		}

		int startTicks;
		int endTicks;
		float startValue;
		float endValue;
		FloatUnaryOperator ease;

		public float get(int animationTicks) {
			int deltaTicks = animationTicks - startTicks;
			float progress = deltaTicks / (float)(endTicks - startTicks);
			progress = ease.apply(progress);
			return progress * (endValue - startValue) + startValue;
		}
	}

	private static final float PI = (float) Math.PI;

	private static FloatUnaryOperator EASE_IN = f -> f * f;
	private static FloatUnaryOperator EASE_IN_STRONG = f -> f * f * f;
	private static FloatUnaryOperator EASE_OUT = f -> 1 - (f - 1) * (f - 1);
	private static FloatUnaryOperator EASE_IN_OUT = f -> f * f * (3 - 2 * f);
	private static FloatUnaryOperator LINEAR = f -> f;

	// just over 6 seconds long
	static final Animation TAIL_SWING = new Animation(0)
			.then(10, PI/2, EASE_OUT)
			.then(15, -PI/2, EASE_IN_OUT)
			.then(15, 3*PI/8, EASE_IN_OUT)
			.then(15, -PI/4, EASE_IN_OUT)
			.then(15, PI/8, EASE_IN_OUT)
			.then(15, -PI/16, EASE_IN_OUT)
			.then(15, PI/32, EASE_IN_OUT)
			.then(15, -PI/64, EASE_IN_OUT)
			//.then(15, PI/128, EASE_IN_OUT) looks fully smooth with this last bit, but I think it's unnatural for the cat.
			.then(8, 0, EASE_IN);

	static final Animation HEAD_ROT = new Animation(0)
			.then(5, PI / 3, EASE_IN_OUT)
			.then(10, 0, EASE_IN)
			.repeat(4);

	static final Animation HEAD_ROT_LONG = new Animation(0)
			.then(5, PI / 3, EASE_IN_OUT)
			.then(10, 0, EASE_IN)
			.repeat(2)
			.then(5, PI / 3, EASE_IN_OUT)
			.then(20, 0, EASE_IN_STRONG)
			.then(5, PI / 3, EASE_IN_OUT)
			.then(10, 0, EASE_IN)
			.then(5, PI / 3, EASE_IN_OUT)
			.then(10, 0, EASE_IN);

	static final int ARM_TRANSITION_DURATION = 12;

	static final Animation RAISE_ARM = new Animation(0)
			.then(ARM_TRANSITION_DURATION, 1, EASE_OUT);

	static final Animation LOWER_ARM = new Animation(1)
			.then(ARM_TRANSITION_DURATION, 0, EASE_OUT);

	@FunctionalInterface
	interface FloatUnaryOperator {
		float apply(float in);
	}
}
