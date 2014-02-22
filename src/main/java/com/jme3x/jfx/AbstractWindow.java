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
	private boolean		init;

	public Region getWindowContent() {
		return this.inner;
	}

	@Override
	protected Region doInit() {
		try {

			this.inner = this.innerInit();
			this.window = new Window("My Window");

			this.window.setResizableWindow(true);
			// prefent layouting errors
			this.window.setResizableBorderWidth(3);

			this.window.setPrefSize(500, 500);
			this.window.getRightIcons().add(new MinimizeIcon(this.window));
			this.window.getRightIcons().add(new CloseIcon(this.window));
			this.innerScroll.setContent(this.inner);
			this.window.getContentPane().getChildren().add(this.innerScroll);

			this.innerScroll.setFitToHeight(true);
			this.innerScroll.setFitToWidth(true);

			this.init = true;
			this.afterInit();
			return this.window;
		} catch (final Throwable t) {
			this.setInnerError(t);
			return null;
		}
	}

	/**
	 * custom init code, eg for setting the enforced settings
	 */
	protected abstract void afterInit();

	/**
	 * JFX Thread only prevents resizing of the window to a smaller value than the content specifies as min, -> no scrollbars will appear
	 * 
	 * @param minimumEnforced
	 */
	public void setEnforceMinimumSize(final boolean minimumEnforced) {
		assert this.init : "Window is not init yet";
		if (minimumEnforced) {
			this.innerScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
			this.innerScroll.setVbarPolicy(ScrollBarPolicy.NEVER);
			this.window.minWidthProperty().bind(this.inner.minWidthProperty());
			this.window.minHeightProperty().bind(this.inner.minHeightProperty());
		} else {
			this.innerScroll.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
			this.innerScroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
			this.window.minHeightProperty().unbind();
			this.window.minWidthProperty().unbind();
		}
	}

	/**
	 * JFX Thread only prevents resizing of the window to a larger value than the content specifies as max(eg if your content cannot be enlarged for technical reasons)
	 * 
	 * @param minimumEnforced
	 */
	public void setEnforceMaximumSize(final boolean maximumEnforced) {
		assert this.init : "Window is not init yet";
		if (maximumEnforced) {
			this.window.maxWidthProperty().bind(this.inner.maxWidthProperty());
			this.window.maxHeightProperty().bind(this.inner.maxHeightProperty());
		} else {
			this.window.maxWidthProperty().unbind();
			this.window.maxHeightProperty().unbind();
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
