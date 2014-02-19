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

public class WindowController {
	private double	dragX;
	private double	dragY;

	@FXML
	ScrollPane		contentPane;
	@FXML
	Button			minimize;
	@FXML
	Button			maximize;
	@FXML
	Button			close;
	@FXML
	HBox			buttonHolder;
	@FXML
	Label			title;
	@FXML
	Region			windowRoot;

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

		this.setupHeaderDragMove();

		this.windowRoot.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {
				System.out.println(mouseEvent);
				WindowController.this.buttonHolder.setCursor(Cursor.N_RESIZE);
			}
		});

		this.windowRoot.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {
				System.out.println(mouseEvent);
				WindowController.this.buttonHolder.setCursor(Cursor.N_RESIZE);
			}
		});
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
