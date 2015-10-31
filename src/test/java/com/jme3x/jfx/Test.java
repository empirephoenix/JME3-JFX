package com.jme3x.jfx;

import org.lwjgl.opengl.Display;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.system.AppSettings;
import com.jme3x.jfx.cursor.proton.ProtonCursorProvider;
import com.jme3x.jfx.window.FXMLWindow;

public class Test extends SimpleApplication {
	private static boolean	assertionsEnabled;

	public static void main(final String[] args) {
		assert Test.enabled();
		if (!Test.assertionsEnabled) {
			//throw new RuntimeException("Assertions must be enabled (vm args -ea");
		}
		final AppSettings settings = new AppSettings(true);
		settings.setRenderer(AppSettings.JOGL_OPENGL_FORWARD_COMPATIBLE);
		settings.setAudioRenderer(AppSettings.JOAL);
		// settings.setGammaCorrection(true);
		final Test t = new Test();
		t.setSettings(settings);
		t.start();
	}

	private static boolean enabled() {
		Test.assertionsEnabled = true;
		return true;
	}

	DisplayInfo displayInfo;

	@Override
	public void simpleInitApp() {

		this.setPauseOnLostFocus(false);
		this.flyCam.setDragToRotate(true);
		this.viewPort.setBackgroundColor(ColorRGBA.Red);

		final GuiManager testguiManager = new GuiManager(this.guiNode, this.assetManager, this, true, new ProtonCursorProvider(this, this.assetManager, this.inputManager));
		/**
		 * 2d gui, use the default input provider
		 */
		this.inputManager.addRawInputListener(testguiManager.getInputRedirector());

		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		final FXMLHud<?> testhud = new FXMLHud<>("com/jme3x/jfx/loading_screen.fxml");
		testhud.precache();
		testguiManager.attachHudAsync(testhud);

		final FXMLWindow<?> testwindow = new FXMLWindow<>("com/jme3x/jfx/loading_screen.fxml");
		testwindow.externalized().set(true);
		testwindow.precache();
		testwindow.titleProperty().set("TestTitle");
		testguiManager.attachHudAsync(testwindow);

		//Display.setResizable(true);
		displayInfo = DisplayInfoProvider.find(this);
	}

	@Override
	public void simpleUpdate(final float tpf) {
		if (displayInfo != null && displayInfo.wasResized().getAndSet(false)) {
			System.out.println("resize");
			// keep settings in sync with the actual Display
			int w = displayInfo.getWidth();
			int h = displayInfo.getHeight();
			if (w < 2) {
				w = 2;
			}
			if (h < 2) {
				h = 2;
			}
			this.settings.setWidth(w);
			this.settings.setHeight(h);
			this.reshape(this.settings.getWidth(), this.settings.getHeight());
		}
	}
}
