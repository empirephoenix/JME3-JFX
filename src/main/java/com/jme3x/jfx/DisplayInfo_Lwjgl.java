package com.jme3x.jfx;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.opengl.Display;

import com.jme3x.jfx.util.os.OperatingSystem;

class DisplayInfo_Lwjgl implements DisplayInfo {
	public static final String PROP_DISPLAY_UNDECORATED = "org.lwjgl.opengl.Window.undecorated";

	private static final Map<String, Point> OFFSET_MAPPING = new HashMap<>();

	static {
		OFFSET_MAPPING.put("Ubuntu 14.04 LTS (trusty)", new Point(10, 37));
		OFFSET_MAPPING.put("Ubuntu 14.04.1 LTS (trusty)", new Point(10, 37));
	}

	/**
	 * Getting the size of the window decorations in the system.
	 */
	public static final Point getWindowDecorationSize() {

		if("true".equalsIgnoreCase(System.getProperty(PROP_DISPLAY_UNDECORATED))) {
			return new Point(0, 0);
		}

		OperatingSystem system = new OperatingSystem();

		if(OFFSET_MAPPING.containsKey(system.getDistribution())) {
			return OFFSET_MAPPING.get(system.getDistribution());
		}

		return new Point(3, 25);
	}

	final AtomicBoolean resized = new AtomicBoolean(false);
	final int insetX;
	final int insetY;

	public DisplayInfo_Lwjgl() {
		super();

		final Point decorationSize = getWindowDecorationSize();
        insetX = (int) decorationSize.getX();
        insetY = (int) decorationSize.getY();
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

	@Override
	public int getInsetX() {
		return insetX;
	}

	@Override
	public int getInsetY() {
		return insetY;
	}
}

