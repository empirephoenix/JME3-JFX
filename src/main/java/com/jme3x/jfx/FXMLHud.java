package com.jme3x.jfx;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.layout.Region;

public class FXMLHud<ControllerType> extends AbstractHud {

	private String			fxml;
	private ControllerType	controller;

	public FXMLHud(final String fxml) {
		this.fxml = fxml;
	}

	@Override
	protected Region innerInit() throws Exception {
		final URL location = Thread.currentThread().getContextClassLoader().getResource(this.fxml);
		FXMLUtils.SHARED_FXML_LOADER.setLocation(location);
		final ResourceBundle defaultRessources = FXMLUtils.SHARED_FXML_LOADER.getResources();
		FXMLUtils.SHARED_FXML_LOADER.setResources(this.addCustomRessources(defaultRessources));
		final Region rv = FXMLUtils.SHARED_FXML_LOADER.load(location.openStream());
		this.controller = FXMLUtils.SHARED_FXML_LOADER.getController();
		assert FXMLUtils.assertInjection(this);
		return rv;
	}

	public ControllerType getController() {
		return this.controller;
	}

	/**
	 * Hook to add own Resourcebundles if necessary
	 * 
	 * @param defaultRessources
	 *            the currently set value
	 * @return
	 */
	protected ResourceBundle addCustomRessources(final ResourceBundle defaultRessources) {
		return defaultRessources;
	}
}
