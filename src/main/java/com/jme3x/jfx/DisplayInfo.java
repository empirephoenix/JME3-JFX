package com.jme3x.jfx;

import java.util.concurrent.atomic.AtomicBoolean;

public interface DisplayInfo {
	int getWidth();
	int getHeight();
	int getX();
	int getY();
	int getInsetX();
	int getInsetY();
	boolean isFullscreen();
	AtomicBoolean wasResized();
}
