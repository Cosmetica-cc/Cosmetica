package com.eyezah.cosmetics.utils;

public enum LoadState {
	LOADING(false),
	LOADED(true),
	FAILED(false),
	RELOADING(true);

	LoadState(boolean loading) {
		this.loading = loading;
	}

	private final boolean loading;

	public boolean isLoading() {
		return this.loading;
	}
}
