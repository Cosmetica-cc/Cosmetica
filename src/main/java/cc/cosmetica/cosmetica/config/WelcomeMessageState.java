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

package cc.cosmetica.cosmetica.config;

import cc.cosmetica.api.VersionInfo;

import java.util.Locale;

public enum WelcomeMessageState {
	FULL,
	SCREEN_ONLY,
	CHAT_ONLY,
	OFF;

	/**
	 * Get whether a chat message for welcome should be shown.
	 *
	 * @param welcomeScreenAllowed whether the welcome screen should be allowed. Generally {@link VersionInfo#megaInvasiveTutorial()} {@code && newPlayer}
	 * @return whether the welcome chat message should be shown.
	 */
	public boolean shouldShowChatMessage(boolean welcomeScreenAllowed) {
		return this == CHAT_ONLY || (this == FULL && !welcomeScreenAllowed);
	}

	/**
	 * Get whether the welcome tutorial should be shown.
	 *
	 * @param welcomeScreenAllowed whether the welcome screen should be allowed. Generally {@link VersionInfo#megaInvasiveTutorial()} {@code && newPlayer}
	 * @return whether the welcome tutorial should be shown.
	 */
	public boolean shouldShowWelcomeTutorial(boolean welcomeScreenAllowed) {
		return welcomeScreenAllowed && (this == FULL || this == SCREEN_ONLY);
	}

	public static WelcomeMessageState of(String string) {
		switch (string.toUpperCase(Locale.ROOT)) {
		case "FULL":
		case "TRUE":
		case "ON":
		default:
			return FULL;
		case "SCREEN_ONLY":
		case "SCREENONLY":
		case "SCREEN ONLY":
		case "SCREEN-ONLY":
			return SCREEN_ONLY;
		case "CHAT_ONLY":
		case "CHATONLY":
		case "CHAT ONLY":
		case "CHAT-ONLY":
			return CHAT_ONLY;
		case "OFF":
		case "FALSE":
			return OFF;
		}
	}
}
