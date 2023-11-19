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

package cc.cosmetica.cosmetica;

import cc.cosmetica.cosmetica.utils.SpecialKeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import java.util.List;

public class CosmeticaKeybinds {
	public static final String COSMETICA_CATEGORY = "key.categories.cosmetica";

	public static KeyMapping openCustomiseScreen;
	public static KeyMapping snipe;

	public static void registerKeyMappings(List<KeyMapping> keymappings) {
		keymappings.add(openCustomiseScreen = new SpecialKeyMapping(
				"key.cosmetica.customise",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_RSHIFT, // not bound by default
				COSMETICA_CATEGORY
		));

		keymappings.add(snipe = new SpecialKeyMapping(
				"key.cosmetica.snipe",
				InputConstants.Type.MOUSE,
				InputConstants.MOUSE_BUTTON_MIDDLE, // not bound by default
				COSMETICA_CATEGORY
		));
	}
}
