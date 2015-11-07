package com.jme3x.jfx;

import java.util.concurrent.atomic.AtomicBoolean;

import com.jme3.system.AppSettings;

class DisplayInfo_AppSettings implements DisplayInfo {
	final AtomicBoolean resized = new AtomicBoolean(false);
	final AppSettings settings;
	final int insetX;
	final int insetY;


	public DisplayInfo_AppSettings(AppSettings settings) {
		super();
		this.settings = settings;

        insetX = 0;
        insetY = 0;
	}

	@Override
	public int getWidth() {
		return settings.getWidth();
	}

	@Override
	public int getHeight() {
		return settings.getHeight();
	}

	/**
	 * Always return 0; (not supported by avoid throwing exception)
	 */
	@Override
	public int getX() {
		return 0;
	}

	/**
	 * Always return 0; (not supported by avoid throwing exception)
	 */
	@Override
	public int getY() {
		return 0;
	}

	@Override
	public boolean isFullscreen() {
		return settings.isFullscreen();
	}

	@Override
	public AtomicBoolean wasResized() {
		return resized;
	}

	@Override
	public int getInsetX() {
		return insetX;
	}

	@Override
	public int getInsetY() {
		return insetY;
	}
}
