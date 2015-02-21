package com.jme3x.jfx.window;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

import com.jme3x.jfx.FXMLUtils;

public class WindowController {

	@FXML
	public Region		bottomBar;

	@FXML
	public Button		minimize;

	@FXML
	public Region		leftBar;

	@FXML
	public Region		topLeftCorner;

	@FXML
	public Label		title;

	@FXML
	public Button		externalize;

	@FXML
	public Button		maximize;

	@FXML
	public Region		bottomRightBar;

	@FXML
	public Region		rightBar;

	@FXML
	public Region		topRightCorner;

	@FXML
	public BorderPane	contentBorderPane;

	@FXML
	public Region		bottomLeftCorner;

	@FXML
	public Region		topBar;

	@FXML
	public Button		close;

	@FXML
	public void initialize() {
		assert FXMLUtils.checkClassInjection(this);

	}

	public void setWindow(final AbstractWindow abstractWindow) {
		this.contentBorderPane.setCenter(abstractWindow.getWindowContent());
	}

}
