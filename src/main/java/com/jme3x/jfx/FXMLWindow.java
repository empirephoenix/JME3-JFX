package com.jme3x.jfx;

import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.Region;

public class FXMLWindow<ControllerType> extends AbstractWindow {

	private String			fxml;
	private ControllerType	controller;

	public FXMLWindow(final String fxml) {
		this.fxml = fxml;
	}

	public ControllerType getController() {
		return this.controller;
	}

	@Override
	protected Region innerInit() throws Exception {
		final FXMLLoader fxmlLoader = new FXMLLoader();
		final URL location = Thread.currentThread().getContextClassLoader().getResource(this.fxml);
		fxmlLoader.setLocation(location);
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		final Region rv = fxmlLoader.load(location.openStream());
		this.controller = fxmlLoader.getController();
		return rv;
	}

	@Override
	protected void afterInit() {
		this.setEnforceMinimumSize(true);
		this.setEnforceMaximumSize(true);
		this.setSize(600, 300);
	}

}
