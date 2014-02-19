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

	@FXML
	ScrollPane	contentPane;
	@FXML
	Button		minimize;
	@FXML
	Button		maximize;
	@FXML
	Button		close;
	@FXML
	HBox		buttonHolder;
	@FXML
	Label		title;
	@FXML
	Region		windowRoot;

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

		final Delta dragDelta = new Delta();
		this.buttonHolder.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {
				// record a delta distance for the drag and drop operation.
				dragDelta.x = WindowController.this.buttonHolder.getLayoutX() - mouseEvent.getScreenX();
				dragDelta.y = WindowController.this.buttonHolder.getLayoutY() - mouseEvent.getSceneY();
				WindowController.this.buttonHolder.setCursor(Cursor.MOVE);
			}
		});
		this.buttonHolder.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {
				WindowController.this.buttonHolder.setCursor(Cursor.HAND);
			}
		});
		this.buttonHolder.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent mouseEvent) {
				WindowController.this.windowRoot.setTranslateX(mouseEvent.getScreenX() + dragDelta.x);
				WindowController.this.windowRoot.setTranslateY(mouseEvent.getSceneY() + dragDelta.y);
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
