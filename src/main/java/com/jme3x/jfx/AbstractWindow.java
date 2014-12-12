package com.jme3x.jfx;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import jfxtras.scene.control.window.Window;

public abstract class AbstractWindow extends AbstractHud {
	private Region					inner;
	private Window					window;
	private ScrollPane				innerScroll;
	private boolean					init;
	private boolean					maximumEnforced;
	private boolean					minimumEnforced;
	private boolean					minimizeVisible	= true;
	private boolean					modal			= false;
	private boolean					useInnerScroll	= true;
	private String					title			= "";
	private SimpleBooleanProperty	externalisable	= new SimpleBooleanProperty(false);
	private SimpleBooleanProperty	externalized	= new SimpleBooleanProperty();

	public void setMinimizeVisible(final boolean visible) {
		assert !this.init : "Cannot change this after window is precached";
	this.minimizeVisible = visible;
	}

	public String getTitle() {
		// no sync as titles are immutable
		return this.window.titleProperty().get();
	}

	public void setUseInnerScroll(final boolean useInnerScroll) {
		assert !this.init : "Cannot change this after window is precached";
	this.useInnerScroll = useInnerScroll;
	}

	public void setResizeable(final boolean b) {
		this.window.setResizableWindow(b);
	}

	/**
	 * returns the Scrollpane inside the window, or null if none is used
	 * 
	 * @return
	 */
	public ScrollPane getInnerScroll() {
		return this.innerScroll;
	}

	/**
	 * centers a window on the screen, using it's current width and height this method does not work correctly, for some reason
	 */
	@Deprecated
	public void center() {
		assert this.init : "Needs to be init to center";
	final double sceneWidth = this.getNode().getScene().getWidth();
	final double sceneHeight = this.getNode().getScene().getHeight();

	double windowWidth = this.inner.getWidth();
	if (windowWidth == 0) {
		windowWidth = Math.max(this.inner.getPrefWidth(), this.inner.getMinWidth());
	}

	double windowHeight = this.inner.getHeight();
	if (windowHeight == 0) {
		windowHeight = Math.max(this.inner.getPrefHeight(), this.inner.getMinHeight());
	}

	final double newPosx = sceneWidth / 2 - windowWidth / 2;
	final double newPosy = sceneHeight / 2 - windowHeight / 2;
	this.setLayoutX((int) newPosx);
	this.setLayoutY((int) newPosy);

	}

	public SimpleBooleanProperty getExternalized() {
		return this.externalized;
	}

	/**
	 * makes this window modal -> eg before every other window and makes sure it cannot loose focus
	 */
	public void setModal(final boolean value) {
		assert !this.init : "modality must be set before init";
	this.modal = value;
	}

	public void setExternalisable(final boolean externalisable) {
		this.externalisable.set(externalisable);
	}

	public SimpleBooleanProperty getExternalisable() {
		return this.externalisable;
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
			this.window = new Window(this.title);

			this.window.setResizableWindow(true);
			// prefent layouting errors
			this.window.setResizableBorderWidth(3);

			final ExternalizeIcon extIcon = new ExternalizeIcon(this);
			extIcon.setVisible(this.externalisable.get());
			this.externalisable.addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) {
					extIcon.visibleProperty().set(newValue);
				}
			});
			this.window.getRightIcons().add(extIcon);
			if (this.minimizeVisible) {
				this.window.getRightIcons().add(new AdjustedMinimizeIcon(this));
			}
			this.window.getRightIcons().add(new AdjustedCloseIcon(this));
			if (this.useInnerScroll) {
				this.innerScroll = new ScrollPane();
				this.innerScroll.setContent(this.inner);
				this.window.getContentPane().getChildren().add(this.innerScroll);

				this.innerScroll.setFitToHeight(true);
				this.innerScroll.setFitToWidth(true);

			} else {
				this.window.getContentPane().getChildren().add(this.inner);
			}

			this.window.minimizedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) {
					if (!newValue) {
						// restore states
						if (AbstractWindow.this.useInnerScroll) {
							AbstractWindow.this.window.getContentPane().getChildren().add(AbstractWindow.this.innerScroll);
						} else {
							AbstractWindow.this.window.getContentPane().getChildren().add(AbstractWindow.this.inner);
						}
						AbstractWindow.this.applyEnforcedMaximumSize();
						AbstractWindow.this.applyEnforcedMinimumSize();
					}
				}
			});
			// apply preferd width
			this.window.setPrefSize(this.inner.getPrefWidth(), this.inner.getPrefHeight());
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
	public void setSize(final double width, final double height) {
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
		if (!this.isAttached()) {
			return;
		}
		// Override close transition, as it would fuck up the window scales and sizes, making windows reusable impossible <br>
		// TODO create a fixed working transition

		if (this.window.getOnCloseAction() != null) {
			this.window.getOnCloseAction().handle(new ActionEvent(this, this.window));
		}
		this.getResponsibleGuiManager().detachHudAsync(this);
		if (this.window.getOnClosedAction() != null) {
			this.window.getOnClosedAction().handle(new ActionEvent(this, this.window));
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
	this.minimumEnforced = minimumEnforced;
	this.applyEnforcedMinimumSize();
	}

	private void applyEnforcedMinimumSize() {
		if (this.minimumEnforced) {
			if (this.useInnerScroll) {
				this.innerScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
				this.innerScroll.setVbarPolicy(ScrollBarPolicy.NEVER);

			}
			this.window.getContentPane().minWidthProperty().bind(this.inner.minWidthProperty());
			this.window.getContentPane().minHeightProperty().bind(this.inner.minHeightProperty());
		} else {
			if (this.useInnerScroll) {
				this.innerScroll.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
				this.innerScroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
			}
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
			this.setInnerTitle(title);
			this.window.setTitle(title);
		} else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					AbstractWindow.this.setInnerTitle(title);
				}
			});
		}
	}

	private void setInnerTitle(final String title) {
		this.title = title;
		if (this.isInitialized()) {
			AbstractWindow.this.window.setTitle(title);
		}
	}

	public Window getInnerWindow() {
		return this.window;
	}

	public void externalize() {
		final ScrollPane content = this.getInnerScroll();
		this.getInnerWindow().getContentPane().getChildren().remove(content);

		final double width = this.getInnerScroll().widthProperty().get();
		final double height = this.getInnerScroll().heightProperty().get();

		final Parent parent = this.getNode().getParent();
		if (parent instanceof Group) {
			final Group castedParent = (Group) parent;
			castedParent.getChildren().remove(this.getNode());
		}

		final Scene scene = new Scene(content);
		final Stage stage = new Stage(StageStyle.DECORATED);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(final WindowEvent event) {
				if (AbstractWindow.this.getResponsibleGuiManager() != null) {
					AbstractWindow.this.getResponsibleGuiManager().detachHudAsync(AbstractWindow.this);
				}
			}
		});
		stage.setScene(scene);
		stage.setWidth(width);
		stage.setHeight(height);
		stage.show();
	}
}
