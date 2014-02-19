package com.jme3x.jfx;

import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;

/**
 * represents the content of a fullscreen hud,the hud is resized to fit the screen automatically.<br>
 * Content must have a Region(or subclass) at the root level
 * 
 * @author empire
 * 
 */
public abstract class AbstractHud {
	private Throwable	innerError	= null;
	protected Region	node;
	protected boolean	initialized;

	/**
	 * initializes the hud, should load all ressources, fxml parsing ect. Allowed to be called from any thread
	 */
	public final void initialize() {
		assert !this.initialized : "Duplicate init";

		if (Platform.isFxApplicationThread()) {
			this.doInit();
		} else {
			final Semaphore waitForInit = new Semaphore(0);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					AbstractHud.this.doInit();
					waitForInit.release();
				}
			});
			waitForInit.acquireUninterruptibly();
		}
		/**
		 * redirect inner error
		 */
		if (this.innerError != null) {
			throw new RuntimeException("Error init hud", this.innerError);
		}
	}

	public Node getNode() {
		return this.node;
	}

	private void doInit() {
		try {
			AbstractHud.this.node = AbstractHud.this.innerInit();
			AbstractHud.this.node.sceneProperty().addListener(new ChangeListener<Scene>() {
				@Override
				public void changed(final ObservableValue<? extends Scene> observable, final Scene oldValue,
						final Scene newValue) {
					if (newValue == null) {
						AbstractHud.this.node.prefWidthProperty().unbind();
						AbstractHud.this.node.prefHeightProperty().unbind();
					} else {
						AbstractHud.this.node.prefWidthProperty().bind(newValue.widthProperty());
						AbstractHud.this.node.prefHeightProperty().bind(newValue.heightProperty());
					}
				}
			});
			AbstractHud.this.initialized = true;
		} catch (final Throwable t) {
			AbstractHud.this.innerError = t;
		}
	}

	/**
	 * prepare load gui here, runs in jfx thread
	 * 
	 * @return
	 */
	protected abstract Region innerInit() throws Exception;

	public boolean isInitialized() {
		return this.initialized;
	}
}
