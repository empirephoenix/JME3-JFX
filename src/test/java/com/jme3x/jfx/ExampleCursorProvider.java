package com.jme3x.jfx;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.input.InputManager;
import com.jme3x.jfx.cursor.ICursorDisplayProvider;
import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.cursor.CursorType;

/**
 * A cursorProvider that simulates the native JFX one and tries to behave similar,<br>
 * using native cursors and 2D surface logic.
 * 
 * @author empire
 * 
 */
public class ExampleCursorProvider implements ICursorDisplayProvider {
	private ConcurrentHashMap<CursorType, JmeCursor>	cache	= new ConcurrentHashMap<CursorType, JmeCursor>();
	private AssetManager								assetManager;
	private InputManager								inputManager;
	private Application									app;

	public ExampleCursorProvider(final Application app, final AssetManager assetManager, final InputManager inputManager) {
		this.assetManager = assetManager;
		this.inputManager = inputManager;
		this.app = app;
		assetManager.registerLocator("", ClasspathLocator.class);
	}

	@Override
	public void showCursor(final CursorFrame cursorFrame) {
		final CursorType cursorType = cursorFrame.getCursorType();
		System.out.println("Show Cursor " + cursorType);
		final JmeCursor toDisplay = this.cache.get(cursorType);
		if (toDisplay != null) {
			this.app.enqueue(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					ExampleCursorProvider.this.inputManager.setMouseCursor(toDisplay);
					return null;
				}
			});
		}
	}

	@Override
	public void setup(final CursorType ctyp) {
		JmeCursor loaded = null;
		switch (ctyp) {
		case CLOSED_HAND:
			break;
		case CROSSHAIR:
			break;
		case DEFAULT:
			break;
		case DISAPPEAR:
			break;
		case E_RESIZE:
			break;
		case HAND:
			break;
		case H_RESIZE:
			break;
		case IMAGE:
			break;
		case MOVE:
			break;
		case NE_RESIZE:
			break;
		case NONE:
			break;
		case NW_RESIZE:
			break;
		case N_RESIZE:
			break;
		case OPEN_HAND:
			break;
		case SE_RESIZE:
			break;
		case SW_RESIZE:
			break;
		case S_RESIZE:
			break;
		case TEXT:
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor-text.cur");
			break;
		case V_RESIZE:
			break;
		case WAIT:
			break;
		case W_RESIZE:
			break;
		default:
			break;

		}

		if (loaded == null) {
			System.err.println("No cursor provided for " + ctyp);
			loaded = (JmeCursor) this.assetManager.loadAsset("com/jme3x/jfx/cursor-standard.cur");
		}
		this.cache.put(ctyp, loaded);
	}
}
