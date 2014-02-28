package com.jme3x.jfx;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.Region;
import jfxtras.labs.scene.control.window.CloseIcon;
import jfxtras.labs.scene.control.window.Window;

public abstract class AbstractWindow extends AbstractHud {
	private Region		inner;
	private Window		window;
	final ScrollPane	innerScroll		= new ScrollPane();
	private boolean		init;
	private boolean		maximumEnforced;
	private boolean		minimumEnforced;
	private boolean		minimizeVisible	= true;
	private boolean		modal			= false;

	public void setMinimizeVisible(final boolean visible) {
		assert !this.init : "Cannot change this after window is precached";
		this.minimizeVisible = visible;
	}

	/**
	 * makes this window modal -> eg before every other window and makes sure it cannot loose focus
	 */
	public void setModal(final boolean value) {
		assert !this.init : "modality must be set before init";
		this.modal = value;
	}

	public boolean isModal() {
		return this.modal;
	}

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

			if (this.minimizeVisible) {
				this.window.getRightIcons().add(new AdjustedMinimizeIcon(this.window));
			}
			this.window.getRightIcons().add(new CloseIcon(this.window));
			this.innerScroll.setContent(this.inner);
			this.window.getContentPane().getChildren().add(this.innerScroll);

			this.innerScroll.setFitToHeight(true);
			this.innerScroll.setFitToWidth(true);

			this.window.minimizedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue,
						final Boolean newValue) {
					if (!newValue) {
						// restore states
						AbstractWindow.this.window.getContentPane().getChildren().add(AbstractWindow.this.innerScroll);
						AbstractWindow.this.applyEnforcedMaximumSize();
						AbstractWindow.this.applyEnforcedMinimumSize();
					}
				}
			});

			this.init = true;
			this.afterInit();
			return this.window;
		} catch (final Throwable t) {
			this.setInnerError(t);
			return null;
		}
	}

	/**
	 * Any Thread
	 */
	public void setSize(final int width, final int height) {
		assert this.init : "Not init";
		if (Platform.isFxApplicationThread()) {
			this.window.setPrefSize(width, height);
		} else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					AbstractWindow.this.window.setPrefSize(width, height);
				}
			});
		}

	}

	public void close() {
		this.window.close();
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
		this.minimumEnforced = minimumEnforced;
		this.applyEnforcedMinimumSize();
	}

	private void applyEnforcedMinimumSize() {
		if (this.minimumEnforced) {
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
		this.maximumEnforced = maximumEnforced;
		this.applyEnforcedMaximumSize();
	}

	private void applyEnforcedMaximumSize() {
		if (this.maximumEnforced) {
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
