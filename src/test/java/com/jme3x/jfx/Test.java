package com.jme3x.jfx;

import org.lwjgl.opengl.Display;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3x.jfx.cursor.proton.ProtonCursorProvider;

public class Test extends SimpleApplication {

	public static void main(final String[] args) {
		new Test().start();
	}

	@Override
	public void simpleInitApp() {
		this.setPauseOnLostFocus(false);
		this.flyCam.setDragToRotate(true);
		this.viewPort.setBackgroundColor(ColorRGBA.Red);

		final GuiManager testguiManager = new GuiManager(this.guiNode, this.assetManager, this, false,
				new ProtonCursorProvider(this, this.assetManager, this.inputManager));
		/**
		 * 2d gui, use the default input provider
		 */
		this.inputManager.addRawInputListener(testguiManager.getInputRedirector());

		final FXMLHud testhud = new FXMLHud("com/jme3x/jfx/loading_screen.fxml");
		testhud.precache();
		testguiManager.attachHudAsync(testhud);

		final FXMLWindow testwindow = new FXMLWindow("com/jme3x/jfx/loading_screen.fxml");
		testwindow.precache();
		testwindow.setTitleAsync("TestTitle");
		testguiManager.attachHudAsync(testwindow);

		Display.setResizable(true);
	}

	@Override
	public void simpleUpdate(final float tpf) {
		if (Display.wasResized()) {
			// keep settings in sync with the actual Display
			int w = Display.getWidth();
			int h = Display.getHeight();
			if (w < 2) {
				w = 2;
			}
			if (h < 2) {
				h = 2;
			}
			this.settings.setWidth(Display.getWidth());
			this.settings.setHeight(Display.getHeight());
			this.reshape(this.settings.getWidth(), this.settings.getHeight());
		}
	}
}
