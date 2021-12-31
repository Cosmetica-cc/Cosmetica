package com.eyezah.cosmetics;


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
