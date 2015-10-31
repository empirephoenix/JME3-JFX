package com.jme3x.jfx;

import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.opengl.Display;

class DisplayInfo_Lwjgl implements DisplayInfo {
	final AtomicBoolean resized = new AtomicBoolean(false);

	public DisplayInfo_Lwjgl() {
		super();
	}

	@Override
	public int getWidth() {
		return Display.getWidth();
	}

	@Override
	public int getHeight() {
		return Display.getHeight();
	}

	@Override
	public int getX() {
		return Display.getX();
	}

	@Override
	public int getY() {
		return Display.getY();
	}

	@Override
	public boolean isFullscreen() {
		return Display.isFullscreen();
	}

	@Override
	public AtomicBoolean wasResized() {
		resized.set(Display.wasResized());
		return resized;
	}

}

