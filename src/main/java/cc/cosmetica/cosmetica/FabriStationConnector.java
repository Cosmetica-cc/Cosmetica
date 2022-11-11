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

package cc.cosmetica.cosmetica;

import com.eyezah.station.FabriStationAPI;

public class FabriStationConnector {
	public static String getArtist() {
		return FabriStationAPI.getArtist();
	}
	public static String getTitle() {
		return FabriStationAPI.getTitle();
	}
	public static String getFormatted() {
		if (FabriStationAPI.isActive()) {
			return getArtist() + " - " + getTitle();
		} else {
			return "none";
		}
	}
	public static boolean isActive() {
		return FabriStationAPI.isActive();
	}
}
