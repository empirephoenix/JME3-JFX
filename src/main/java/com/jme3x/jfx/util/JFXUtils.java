package com.jme3x.jfx.util;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import com.jme3x.jfx.util.os.OperatingSystem;

/**
 * Set of methods for scrap work JFX.
 * 
 * @author Ronn
 */
public class JFXUtils {

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
}
