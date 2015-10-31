package com.jme3x.jfx;

import java.util.concurrent.atomic.AtomicBoolean;

public interface DisplayInfo {
	int getWidth();
	int getHeight();
	int getX();
	int getY();
	boolean isFullscreen();
	AtomicBoolean wasResized();
}
