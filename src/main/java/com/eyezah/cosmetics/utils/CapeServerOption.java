package com.eyezah.cosmetics.utils;

public enum CapeServerOption {
	SHOW(2, "Show"),
	HIDE(1, "Hide"),
	REPLACE(0, "Replace");

	private int value;
	private String formattedName;

	CapeServerOption(int value, String formattedName) {
		this.value = value;
		this.formattedName = formattedName;
	}

	public int getValue() {
		return value;
	}

	public String getFormattedName() {
		return formattedName;
	}

	public static CapeServerOption getEnumCaseInsensitive(String requirement) {
		if ("show".equalsIgnoreCase(requirement)) return SHOW;
		if ("hide".equalsIgnoreCase(requirement)) return HIDE;
		if ("replace".equalsIgnoreCase(requirement)) return REPLACE;
		return null;
	}
}
