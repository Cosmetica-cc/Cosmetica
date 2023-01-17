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
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * A built-in, live model. Used for some Region Specific Effects.
 */
public interface BuiltInModel {
	void render(PoseStack stack, MultiBufferSource multiBufferSource, Playerish player, boolean left, int packedLight);

	/**
	 * Stores the RSE notices for the IDs of each built-in model.
	 */
	Map<String, Component> NOTICES = new HashMap<>();
}
