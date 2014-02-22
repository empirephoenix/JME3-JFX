package com.jme3x.jfx;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.Region;
import jfxtras.labs.scene.control.window.CloseIcon;
import jfxtras.labs.scene.control.window.MinimizeIcon;
import jfxtras.labs.scene.control.window.Window;

public abstract class AbstractWindow extends AbstractHud {
	private Region		inner;
	private Window		window;
	final ScrollPane	innerScroll	= new ScrollPane();

	public Region getWindowContent() {
		return this.inner;
	}

	@Override
	protected Region doInit() {
		try {
			this.inner = this.innerInit();

			this.window = new Window("My Window");
			this.window.setResizableWindow(true);
			this.window.maxWidthProperty().bind(this.inner.maxWidthProperty());
			this.window.maxHeightProperty().bind(this.inner.maxHeightProperty());
			// prefent layouting errors
			this.window.setResizableBorderWidth(3);

			this.window.setPrefSize(500, 500);
			this.window.getRightIcons().add(new MinimizeIcon(this.window));
			this.window.getRightIcons().add(new CloseIcon(this.window));
			this.innerScroll.setContent(this.inner);
			this.window.getContentPane().getChildren().add(this.innerScroll);

			this.innerScroll.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);

			return this.window;
		} catch (final Throwable t) {
			this.setInnerError(t);
			return null;
		}
	}

	/**
	 * can be called from any Thread, if not jfx thread is async
	 * 
	 * @param title
	 */
	public void setLayoutX(final int x) {
		if (Platform.isFxApplicationThread()) {
			this.window.setLayoutX(x);
		} else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					AbstractWindow.this.window.setLayoutX(x);
				}
			});
		}
	}

	/**
	 * can be called from any Thread, if not jfx thread is async
	 * 
	 * @param title
	 */
	public void setLayoutY(final int y) {
		if (Platform.isFxApplicationThread()) {
			this.window.setLayoutY(y);
		} else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					AbstractWindow.this.window.setLayoutY(y);
				}
			});
		}
	}

	/**
	 * sets the window title, can be called from any Thread, if not jfx thread is async
	 * 
	 * @param title
	 */
	public void setTitleAsync(final String title) {
		if (Platform.isFxApplicationThread()) {
			this.window.setTitle(title);
		} else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					AbstractWindow.this.window.setTitle(title);
				}
			});
		}
	}
}
