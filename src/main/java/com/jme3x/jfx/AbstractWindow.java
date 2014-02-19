package com.jme3x.jfx;

import java.net.URL;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

public abstract class AbstractWindow extends AbstractHud {
	private BorderPane			windowLayout;
	private Region				inner;
	private WindowController	windowController;

	public void setInner(final Region content) {
		this.windowLayout.setCenter(this.windowLayout);
	}

	public Region getWindowContent() {
		return this.inner;
	}

	@Override
	protected Region doInit() {
		try {
			this.inner = this.innerInit();
			final FXMLLoader fxmlLoader = new FXMLLoader();
			final URL location = this.getClass().getResource("window.fxml");
			fxmlLoader.setLocation(location);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			this.windowLayout = fxmlLoader.load(location.openStream());
			this.windowController = fxmlLoader.getController();
			this.setContent(this.inner);
			return this.windowLayout;
		} catch (final Throwable t) {
			this.setInnerError(t);
			return null;
		}
	}

	public void setContent(final Region content) {
		this.windowController.setContent(content);
	}

	/**
	 * sets the window title, can be called from any Thread, if not jfx thread is async
	 * 
	 * @param title
	 */
	public void setTitleAsync(final String title) {
		if (Platform.isFxApplicationThread()) {
			this.windowController.setTitle(title);
		} else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					AbstractWindow.this.windowController.setTitle(title);
				}
			});
		}
	}
}
