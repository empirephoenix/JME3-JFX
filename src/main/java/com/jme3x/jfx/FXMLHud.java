package com.jme3x.jfx;

import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.Region;

public class FXMLHud extends AbstractHud {

	private String	fxml;

	public FXMLHud(final String fxml) {
		this.fxml = fxml;
	}

	@Override
	protected Region innerInit() throws Exception {
		final FXMLLoader fxmlLoader = new FXMLLoader();
		final URL location = Thread.currentThread().getContextClassLoader().getResource(this.fxml);
		fxmlLoader.setLocation(location);
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		return fxmlLoader.load(location.openStream());
	}

}
