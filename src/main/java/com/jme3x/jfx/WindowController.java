package com.jme3x.jfx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class WindowController {
	private final static int	borderSize	= 2;
	private double				dragX;
	private double				dragY;

	@FXML
	ScrollPane					contentPane;
	@FXML
	Button						minimize;
	@FXML
	Button						maximize;
	@FXML
	Button						close;
	@FXML
	HBox						buttonHolder;
	@FXML
	Label						title;
	@FXML
	Region						windowRoot;

	@FXML
	StackPane					spane;
	protected boolean			pressed;
	protected EBorder			resizeDir;
	protected double			initialSizeX;
	protected double			initialSizeY;
	protected double			initPosX;
	protected double			initPosY;

	@FXML
	private void initialize() {
		this.close.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				final Group parent = (Group) WindowController.this.windowRoot.getParent();
				// TODO closeAllowed/dispose callback
				parent.getChildren().remove(WindowController.this.windowRoot);
			}
		});
		//
		this.setupHeaderDragMove();

		this.windowRoot.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(final MouseEvent event) {
				if (WindowController.this.resizeDir != EBorder.None) {
					WindowController.this.pressed = true;
					WindowController.this.dragX = event.getSceneX();
					WindowController.this.dragY = event.getSceneY();
					WindowController.this.initialSizeX = WindowController.this.windowRoot.getWidth();
					WindowController.this.initialSizeY = WindowController.this.windowRoot.getHeight();
					WindowController.this.initPosX = WindowController.this.windowRoot.getTranslateX();
					WindowController.this.initPosY = WindowController.this.windowRoot.getTranslateY();
					System.out.println("Starting resize " + WindowController.this.resizeDir);
				}
			}
		});

		this.windowRoot.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				WindowController.this.pressed = false;
			}
		});

		this.windowRoot.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(final MouseEvent mouseEvent) {
				if (WindowController.this.pressed) {
					System.out.println(mouseEvent);
					final double difX = mouseEvent.getSceneX() - WindowController.this.dragX;
					final double difY = mouseEvent.getSceneY() - WindowController.this.dragY;
					System.out.println(difX + "," + difY);

					switch (WindowController.this.resizeDir) {
					case East:
						WindowController.this.resizeEast(difX);
						break;
					case None:
						break;
					case North:
						WindowController.this.resizeNorth(difY);
						break;
					case NorthEast:
						break;
					case NorthWest:
						break;
					case South:
						WindowController.this.resizeSouth(difY);
						break;
					case SouthEast:
						break;
					case SouthWest:
						break;
					case West:
						WindowController.this.resizeWest(difX);
						break;
					}
				}
			}
		});

		this.windowRoot.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {
				WindowController.this.resizeDir = WindowController.this.determineBorder(mouseEvent);
				switch (WindowController.this.resizeDir) {
				case NorthWest:
					WindowController.this.windowRoot.setCursor(Cursor.NW_RESIZE);
					break;
				case East:
					WindowController.this.windowRoot.setCursor(Cursor.E_RESIZE);
					break;
				case None:
					break;
				case North:
					WindowController.this.windowRoot.setCursor(Cursor.N_RESIZE);
					break;
				case NorthEast:
					WindowController.this.windowRoot.setCursor(Cursor.NE_RESIZE);
					break;
				case South:
					WindowController.this.windowRoot.setCursor(Cursor.S_RESIZE);
					break;
				case SouthEast:
					WindowController.this.windowRoot.setCursor(Cursor.SE_RESIZE);
					break;
				case SouthWest:
					WindowController.this.windowRoot.setCursor(Cursor.SW_RESIZE);
					break;
				case West:
					WindowController.this.windowRoot.setCursor(Cursor.W_RESIZE);
					break;
				}

			}
		});

	}

	protected void resizeWest(final double difX) {
		final double newWidth = WindowController.this.initialSizeX - difX;
		if (newWidth < WindowController.this.windowRoot.minWidthProperty().doubleValue()) {
			return;
		}
		if (newWidth > WindowController.this.windowRoot.maxWidthProperty().doubleValue()) {
			return;
		}
		WindowController.this.windowRoot.setTranslateX(WindowController.this.initPosX + difX);
		WindowController.this.windowRoot.setPrefWidth(newWidth);
	}

	protected void resizeEast(final double difX) {
		final double newWidth = WindowController.this.initialSizeX + difX;
		if (newWidth < WindowController.this.windowRoot.minWidthProperty().doubleValue()) {
			return;
		}
		if (newWidth > WindowController.this.windowRoot.maxWidthProperty().doubleValue()) {
			return;
		}
		WindowController.this.windowRoot.setPrefWidth(newWidth);
	}

	protected void resizeSouth(final double difY) {
		final double newHeight = WindowController.this.initialSizeY + difY;
		if (newHeight < WindowController.this.windowRoot.minHeightProperty().doubleValue()) {
			return;
		}
		if (newHeight > WindowController.this.windowRoot.maxHeightProperty().doubleValue()) {
			return;
		}
		WindowController.this.windowRoot.setPrefHeight(newHeight);
	}

	protected void resizeNorth(final double difY) {
		final double newHeight = WindowController.this.initialSizeY - difY;
		if (newHeight < WindowController.this.windowRoot.minHeightProperty().doubleValue()) {
			return;
		}
		if (newHeight > WindowController.this.windowRoot.maxHeightProperty().doubleValue()) {
			return;
		}
		WindowController.this.windowRoot.setPrefHeight(newHeight);
		WindowController.this.windowRoot.setTranslateY(WindowController.this.initPosY + difY);

	}

	private EBorder determineBorder(final MouseEvent mouseEvent) {
		final double left = mouseEvent.getSceneX() - WindowController.this.windowRoot.getTranslateX();
		final double right = mouseEvent.getSceneX()
				- (WindowController.this.windowRoot.getTranslateX() + (WindowController.this.windowRoot.getWidth() - WindowController.borderSize));
		final double up = mouseEvent.getSceneY() - WindowController.this.windowRoot.getTranslateY();
		final double down = mouseEvent.getSceneY()
				- (WindowController.this.windowRoot.getTranslateY() + (WindowController.this.windowRoot.getHeight() - WindowController.borderSize));
		if (left < WindowController.borderSize && left > 0) {
			if (up < WindowController.borderSize && up > 0) {
				return EBorder.NorthWest;
			} else if (down < WindowController.borderSize && down > 0) {
				return EBorder.SouthWest;
			} else {
				return EBorder.West;
			}
		} else if (right < WindowController.borderSize && right > 0) {
			if (up < WindowController.borderSize && up > 0) {
				return EBorder.NorthEast;
			} else if (down < WindowController.borderSize && down > 0) {
				return EBorder.SouthEast;
			} else {
				return EBorder.East;
			}
		} else if (up < WindowController.borderSize && up > 0) {
			return EBorder.North;
		} else if (down < WindowController.borderSize && down > 0) {
			return EBorder.South;
		}
		return EBorder.None;
	}

	private void setupHeaderDragMove() {
		this.buttonHolder.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {
				// record a delta distance for the drag and drop operation.
				WindowController.this.dragX = WindowController.this.windowRoot.getTranslateX()
						- mouseEvent.getScreenX();
				WindowController.this.dragY = WindowController.this.windowRoot.getTranslateY()
						- mouseEvent.getScreenY();
				WindowController.this.buttonHolder.setCursor(Cursor.MOVE);
			}
		});
		this.buttonHolder.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {
				WindowController.this.buttonHolder.setCursor(Cursor.HAND);
				WindowController.this.dragX = 0;
				WindowController.this.dragY = 0;
			}
		});
		this.buttonHolder.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {
				WindowController.this.windowRoot.setTranslateX(mouseEvent.getScreenX() + WindowController.this.dragX);
				WindowController.this.windowRoot.setTranslateY(mouseEvent.getScreenY() + WindowController.this.dragY);
			}
		});
		this.buttonHolder.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {
				WindowController.this.buttonHolder.setCursor(Cursor.HAND);
			}
		});
	}

	public void setTitle(final String title) {
		this.title.setText(title);
	}

	public void setContent(final Region inner) {
		this.contentPane.setContent(inner);
	}

}
