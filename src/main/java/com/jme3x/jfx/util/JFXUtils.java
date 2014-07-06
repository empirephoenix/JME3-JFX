package com.jme3x.jfx.util;

import java.awt.Insets;
import java.awt.Point;

import javax.swing.JFrame;

/**
 * Set of methods for scrap work JFX.
 * 
 * @author Ronn
 */
public class JFXUtils {

	/**
	 * Getting the size of the window decorations in the system.
	 */
	public static final Point getWindowDecorationSize() {

		JFrame frame = new JFrame();
		frame.setSize(0, 0);
		frame.setVisible(true);

		final Insets insets = frame.getInsets();

		frame.setVisible(false);
		frame.dispose();

		Point point = new Point();
		point.setLocation(insets.right + insets.left, insets.top + insets.bottom);

		return point;
	}
}
