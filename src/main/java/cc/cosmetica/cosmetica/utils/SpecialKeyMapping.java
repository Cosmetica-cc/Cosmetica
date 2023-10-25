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

package cc.cosmetica.cosmetica.utils;

import cc.cosmetica.cosmetica.mixin.keys.KeymappingAccessor;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import java.util.Map;

// like a key mapping but better
public class SpecialKeyMapping extends KeyMapping {
	public SpecialKeyMapping(String id, InputConstants.Type type, int key, String category) {
		super(id, type, InputConstants.UNKNOWN.getValue(), category);
		this.setKey(type.getOrCreate(key));
	}

	private static final Map<InputConstants.Key, KeyMapping> MAP = Maps.newHashMap(); // not worth making it a list for 2 keys

	public static void clearMappings() {
		MAP.clear();
	}

	public static KeyMapping putMapping(InputConstants.Key key, KeyMapping keyMapping) {
		return MAP.put(key, keyMapping);
	}

	// reimplement vanilla code identically aside from it uses our map.

	public static void click(InputConstants.Key key) {
		KeyMapping keyMapping = MAP.get(key);

		if (keyMapping != null) {
			((KeymappingAccessor) keyMapping).setClickCount(((KeymappingAccessor) keyMapping).getClickCount() + 1); // As is evident, this is clearly far more readable than ++keyMapping.clickCount. Would definitely not increase legibility to use an AW instead. Most definitely not.
		}
	}

	public static void set(InputConstants.Key key, boolean bl) {
		KeyMapping keyMapping = MAP.get(key);

		if (keyMapping != null) {
			keyMapping.setDown(bl);
		}
	}
}
