package com.jme3x.jfx.window;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

import com.jme3x.jfx.FXMLUtils;

public class WindowController {

	@FXML
	public Region			bottomBar;

	@FXML
	public Button			minimize;

	@FXML
	public Region			leftBar;

	@FXML
	public Region			topLeftCorner;

	@FXML
	public Label			title;

	@FXML
	public Button			externalize;

	@FXML
	public Button			maximize;

	@FXML
	public Region			bottomRightBar;

	@FXML
	public Region			rightBar;

	@FXML
	public Region			topRightCorner;

	@FXML
	public BorderPane		contentBorderPane;

	@FXML
	public Region			bottomLeftCorner;

	@FXML
	public Region			titleBar;

	@FXML
	public Region			topBar;

	@FXML
	public Button			close;

	private AbstractWindow	window;

	@FXML
	public void initialize() {
		assert FXMLUtils.checkClassInjection(this);

	}

	public void setWindow(final AbstractWindow abstractWindow) {
		this.contentBorderPane.setCenter(abstractWindow.getWindowContent());
		this.window = abstractWindow;

		this.initDragging();
		this.initResize(this.topBar, Cursor.N_RESIZE);
		this.initResize(this.topLeftCorner, Cursor.NW_RESIZE);
		this.initResize(this.topRightCorner, Cursor.NE_RESIZE);

		this.initResize(this.bottomBar, Cursor.S_RESIZE);
		this.initResize(this.bottomLeftCorner, Cursor.SW_RESIZE);
		this.initResize(this.bottomRightBar, Cursor.SE_RESIZE);

		this.initResize(this.leftBar, Cursor.W_RESIZE);
		this.initResize(this.bottomLeftCorner, Cursor.SW_RESIZE);
		this.initResize(this.topLeftCorner, Cursor.NW_RESIZE);

		this.initResize(this.rightBar, Cursor.E_RESIZE);
		this.initResize(this.bottomRightBar, Cursor.SE_RESIZE);
		this.initResize(this.topRightCorner, Cursor.NE_RESIZE);

	}

	private void initResize(Region draggable, Cursor cursor) {
		boolean resizeable = true;
		final Vector2d initialMousePos = new Vector2d();
		final Vector2d initialSize = new Vector2d();
		final Vector2d initialPos = new Vector2d();
		draggable.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (resizeable) {
					initialSize.x = WindowController.this.window.getNode().getWidth();
					initialSize.y = WindowController.this.window.getNode().getHeight();

					initialPos.x = WindowController.this.window.getNode().getLayoutX();
					initialPos.y = WindowController.this.window.getNode().getLayoutY();

					initialMousePos.x = mouseEvent.getSceneX();
					initialMousePos.y = mouseEvent.getSceneY();
					draggable.setCursor(cursor);
				}
			}
		});
		draggable.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (resizeable) {
					draggable.setCursor(cursor);
				}
			}
		});
		draggable.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (resizeable) {
					WindowController.this.resize(cursor, initialMousePos, new Vector2d(mouseEvent.getSceneX(), mouseEvent.getSceneY()), initialSize, initialPos);
				}
			}
		});
		draggable.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (resizeable) {
					draggable.setCursor(cursor);
				}
			}
		});
	}

	protected void resize(Cursor cursor, Vector2d initialMousePos, Vector2d currentMousePos, Vector2d initialSize, Vector2d initialPos) {
		Vector2d mouseDelta = currentMousePos.subtract(initialMousePos);
		double actualMinimumSizeY = this.bottomBar.getHeight() + this.titleBar.getHeight() + this.topBar.getHeight() + this.window.getWindowContent().minHeight(initialSize.x);
		double actualMinimumSizeX = this.leftBar.getWidth() + this.rightBar.getWidth() + this.window.getWindowContent().minWidth(initialSize.y);

		if (cursor == Cursor.E_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.SE_RESIZE) {
			double newXSize = initialSize.x + mouseDelta.x;
			// // do not allow resizing to small, except there is a scrollpane embedded

			if (this.window.getNode().getLayoutX() + newXSize < this.window.getNode().getScene().getWidth() && newXSize >= actualMinimumSizeX && !this.window.innerScrollProperty().get() && currentMousePos.getX() >= 0) {
				this.window.getNode().setMinWidth(Math.max(newXSize, 5));
				this.window.getNode().setMaxWidth(Math.max(newXSize, 5));
			}

		}

		if (cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.SW_RESIZE) {
			double newXSize = initialSize.x - mouseDelta.x;
			// // do not allow resizing to small, except there is a scrollpane embedded

			if (initialPos.x + mouseDelta.x >= 0 && newXSize >= actualMinimumSizeX && !this.window.innerScrollProperty().get() && currentMousePos.getX() >= 0) {
				this.window.getNode().setMinWidth(Math.max(newXSize, 5));
				this.window.getNode().setMaxWidth(Math.max(newXSize, 5));
				this.window.setLayoutX(initialPos.x + mouseDelta.x);
			}

		}

		if (cursor == Cursor.N_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.NW_RESIZE) {
			double newYSize = initialSize.y - mouseDelta.y;
			// // do not allow resizing to small, except there is a scrollpane embedded
			//
			if (initialPos.y + mouseDelta.y >= 0 && newYSize >= actualMinimumSizeY && !this.window.innerScrollProperty().get() && currentMousePos.getY() >= 0) {
				this.window.getNode().setMinHeight(Math.max(newYSize, 5));
				this.window.getNode().setMaxHeight(Math.max(newYSize, 5));
				this.window.setLayoutY(initialPos.y + mouseDelta.y);
			}

		}

		if (cursor == Cursor.S_RESIZE || cursor == Cursor.SE_RESIZE || cursor == Cursor.SW_RESIZE) {
			double newYSize = initialSize.y + mouseDelta.y;

			// // do not allow resizing to small, except there is a scrollpane embedded
			//
			if (this.window.getNode().getLayoutY() + newYSize < this.window.getNode().getScene().getHeight() && newYSize >= actualMinimumSizeY && !this.window.innerScrollProperty().get() && currentMousePos.getY() >= 0) {
				this.window.getNode().setMinHeight(Math.max(newYSize, 5));
				this.window.getNode().setMaxHeight(Math.max(newYSize, 5));
			}

		}

		// sanity checks,
		// prevent in this case to resize a window so, that the titlebar is no longer reachable
		if (this.window.getNode().getLayoutY() < 0) {
			this.window.getNode().setLayoutY(0);
		}
	}

	private void initDragging() {
		boolean move = this.window.moveAbleProperty().get();
		final Vector2d dragDelta = new Vector2d();
		this.titleBar.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (move) {
					dragDelta.x = WindowController.this.window.getNode().getLayoutX() - mouseEvent.getSceneX();
					dragDelta.y = WindowController.this.window.getNode().getLayoutY() - mouseEvent.getSceneY();
					WindowController.this.titleBar.setCursor(Cursor.MOVE);
				}
			}
		});
		this.titleBar.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (move) {
					WindowController.this.titleBar.setCursor(Cursor.HAND);
				}
			}
		});
		this.titleBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (move) {
					if (mouseEvent.getSceneX() < 0) {
						return;
					}
					if (mouseEvent.getSceneY() < 0) {
						return;
					}

					if (mouseEvent.getSceneX() > WindowController.this.window.getNode().getScene().getWidth()) {
						return;
					}
					if (mouseEvent.getSceneY() > WindowController.this.window.getNode().getScene().getHeight()) {
						return;
					}
					WindowController.this.window.getNode().setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
					WindowController.this.window.getNode().setLayoutY(Math.max(mouseEvent.getSceneY() + dragDelta.y, 0));
				}
			}
		});
		this.titleBar.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (move) {
					WindowController.this.titleBar.setCursor(Cursor.HAND);
				}
			}
		});
	}

}
