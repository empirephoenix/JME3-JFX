package com.jme3.system.jogl;

import java.util.concurrent.atomic.AtomicBoolean;

import com.jme3x.jfx.DisplayInfo;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;

public class DisplayInfo_Jogl implements DisplayInfo {
	final AtomicBoolean resized = new AtomicBoolean(false);
	final GLWindow canvas;

	public DisplayInfo_Jogl(JoglNewtAbstractDisplay d) {
		this(d.canvas);
	}

	public DisplayInfo_Jogl(GLWindow canvas) {
		super();
		this.canvas = canvas;
		canvas.addWindowListener(new WindowListener() {

			@Override
			public void windowResized(WindowEvent e) {
				resized.set(true);
			}

			@Override
			public void windowRepaint(WindowUpdateEvent e) {
			}

			@Override
			public void windowMoved(WindowEvent e) {
			}

			@Override
			public void windowLostFocus(WindowEvent e) {
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
			}

			@Override
			public void windowDestroyed(WindowEvent e) {
			}

			@Override
			public void windowDestroyNotify(WindowEvent e) {
			}
		});
	}

	@Override
	public int getWidth() {
		return canvas.getSurfaceWidth();// + canvas.getInsets().getTotalWidth();
	}

	@Override
	public int getHeight() {
		return canvas.getSurfaceHeight();// + canvas.getInsets().getTotalHeight();
	}

	@Override
	public int getX() {
		return canvas.convertToPixelUnits(new int[]{canvas.getX(), 0})[0];
	}

	@Override
	public int getY() {
		return canvas.convertToPixelUnits(new int[]{0, canvas.getY()})[1];
	}

	@Override
	public boolean isFullscreen() {
		return canvas.isFullscreen();
	}

	@Override
	public AtomicBoolean wasResized() {
		return resized;
	}

	@Override
	public int getInsetX() {
		return canvas.convertToPixelUnits(new int[]{canvas.getInsets().getLeftWidth(), 0})[0];
	}

	@Override
	public int getInsetY() {
		return canvas.convertToPixelUnits(new int[]{0, canvas.getInsets().getTopHeight()})[1];
	}
}
