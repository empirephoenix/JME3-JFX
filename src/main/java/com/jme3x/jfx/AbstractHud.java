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
	private boolean		initialized;

	/**
	 * initializes the hud, should load all ressources, fxml parsing ect. Allowed to be called from any thread,<br>
	 * blocks till jfx thread is finished with work
	 */
	public final void precache() {
		assert !this.initialized : "Duplicate init";

		if (Platform.isFxApplicationThread()) {
			this.node = this.doInit();
		} else {
			final Semaphore waitForInit = new Semaphore(0);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					AbstractHud.this.node = AbstractHud.this.doInit();
					waitForInit.release();
				}
			});
			waitForInit.acquireUninterruptibly();
		}
		/**
		 * redirect inner error
		 */
		if (this.getInnerError() != null) {
			throw new RuntimeException("Error init hud", this.getInnerError());
		}
		AbstractHud.this.initialized = true;
	}

	public Node getNode() {
		return this.node;
	}

	protected Region doInit() {
		try {
			final Region node = AbstractHud.this.innerInit();
			node.sceneProperty().addListener(new ChangeListener<Scene>() {
				@Override
				public void changed(final ObservableValue<? extends Scene> observable, final Scene oldValue,
						final Scene newValue) {
					if (newValue == null) {
						node.prefWidthProperty().unbind();
						node.prefHeightProperty().unbind();
					} else {
						node.prefWidthProperty().bind(newValue.widthProperty());
						node.prefHeightProperty().bind(newValue.heightProperty());
					}
				}
			});
			return node;
		} catch (final Throwable t) {
			AbstractHud.this.setInnerError(t);
			return null;
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

	public Throwable getInnerError() {
		return this.innerError;
	}

	protected void setInnerError(final Throwable innerError) {
		this.innerError = innerError;
	}
}
