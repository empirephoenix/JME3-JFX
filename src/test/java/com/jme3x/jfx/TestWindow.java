package com.jme3x.jfx;

import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.Region;

public class TestWindow extends AbstractWindow {

	@Override
	protected Region innerInit() throws Exception {
		final FXMLLoader fxmlLoader = new FXMLLoader();
		final URL location = this.getClass().getResource("loading_screen.fxml");
		fxmlLoader.setLocation(location);
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

		return fxmlLoader.load(location.openStream());
	}

	@Override
	protected void afterInit() {
		this.setEnforceMinimumSize(true);
		this.setEnforceMaximumSize(true);
	}

}
