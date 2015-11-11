package com.jme3x.jfx;

import java.util.concurrent.atomic.AtomicBoolean;

public interface DisplayInfo {
	/** return width in pixel */
	int getWidth();
	/** return height in pixel */
	int getHeight();
	/** return x in pixel */
	int getX();
	/** return y in pixel */
	int getY();
	/** return inset of left in pixel */
	int getInsetX();
	/** return inset of top in pixel */
	int getInsetY();
	boolean isFullscreen();
	AtomicBoolean wasResized();
}
