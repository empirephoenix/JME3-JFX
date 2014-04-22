package com.jme3x.jfx;

import java.net.URL;
import java.util.ResourceBundle;

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
		final ResourceBundle ressources = fxmlLoader.getResources();
		fxmlLoader.setResources(this.addCustomRessources(ressources));
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		final Region rv = fxmlLoader.load(location.openStream());
		this.controller = fxmlLoader.getController();
		assert FXMLUtils.assertInjection(this);
		return rv;
	}

	/**
	 * Hook to add own Resourcebundles if necessary
	 * 
	 * @param ressources
	 *            the currently set value
	 * @return
	 */
	protected ResourceBundle addCustomRessources(final ResourceBundle ressources) {
		return ressources;
	}

	@Override
	protected void afterInit() {
		this.setEnforceMinimumSize(true);
		this.setEnforceMaximumSize(true);
	}

}
