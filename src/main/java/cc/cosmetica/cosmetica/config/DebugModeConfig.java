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

package cc.cosmetica.cosmetica.config;

import com.google.gson.annotations.SerializedName;

/**
 * GSON config for dev mode.
 */
public class DebugModeConfig {
	@SerializedName("elevate_debug_logging") public boolean elevateDebugLogging = true;
	@SerializedName("log_urls") public boolean logURLs = false;
	@SerializedName("debug_commands") public boolean debugCommands = false;

	@SerializedName("image_dumping") public ImageDumpingSettings imageDumpingSettings = new ImageDumpingSettings();

	@SerializedName("hat") public TestModelConfig hat = new TestModelConfig("hat");
	@SerializedName("left_shoulder_buddy") public TestModelConfig leftShoulderBuddy = new TestModelConfig("shoulder_buddy");
	@SerializedName("right_shoulder_buddy") public TestModelConfig rightShoulderBuddy = new TestModelConfig("shoulder_buddy");
	@SerializedName("back_bling") public TestModelConfig backBling = new TestModelConfig("back_bling");
	@SerializedName("cape") public TestCapeConfig cape = new TestCapeConfig();

	public static class ImageDumpingSettings {
		public ImageDumpingSettings() {
			this.textureLoading = false;
			this.capeModifications = false;
		}

		@SerializedName("texture_loading") public boolean textureLoading;
		@SerializedName("cape_modifications") public boolean capeModifications;

		public boolean either() {
			return this.textureLoading || this.capeModifications;
		}
	}

	public static class TestModelConfig {
		public TestModelConfig(String location) {
			this.location = location;
		}

		// GSON might need this
		public TestModelConfig() {
		}

		public String location = "";
		public int flags = 0;
	}

	public static class TestCapeConfig {
		public String location = "cape";
		@SerializedName("frame_delay") public int frameDelay = 50;
	}
}
