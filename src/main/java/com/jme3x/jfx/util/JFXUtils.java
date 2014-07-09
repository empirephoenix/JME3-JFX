package com.jme3x.jfx.util;

import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

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

		final Point point = new Point();
		final AtomicBoolean sync = new AtomicBoolean(false);

		SwingUtilities.invokeLater(() -> {

			final Frame frame = new Frame();
			frame.setVisible(true);
			try {

				// wait AWT init native minimum size
				try {
					Thread.sleep(500);
				} catch(Exception e) {
					e.printStackTrace();
				}

				final Rectangle bounds = frame.getBounds();
				point.setLocation(bounds.getWidth(), bounds.getHeight());

				frame.setVisible(false);
				frame.dispose();

			} finally {
				synchronized(point) {
					sync.set(true);
					point.notifyAll();
				}
			}
		});

		synchronized(point) {
			if(!sync.get()) {
				try {
					point.wait();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		return point;
	}
}
