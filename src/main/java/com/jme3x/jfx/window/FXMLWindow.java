package com.jme3x.jfx.window;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

import com.jme3x.jfx.FXMLUtils;

import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.Region;

public class FXMLWindow<ControllerType> extends AbstractWindow {

	private ControllerType	controller;
	private URL				location;
	InputStream				inStream;

	public FXMLWindow(final String fxml) {
		this.location = Thread.currentThread().getContextClassLoader().getResource(fxml);
		try {
			this.inStream = this.location.openStream();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * the given stream will be closed!
	 * 
	 * @param location
	 * @param inStream
	 */
	public FXMLWindow(final URL location, final InputStream inStream) {
		super();
		this.location = location;
		this.inStream = inStream;
	}

	public ControllerType getController() {
		return this.controller;
	}

	@Override
	protected Region innerInit() throws Exception {
		FXMLUtils.SHARED_FXML_LOADER.setLocation(this.location);
		final ResourceBundle ressources = FXMLUtils.SHARED_FXML_LOADER.getResources();
		FXMLUtils.SHARED_FXML_LOADER.setResources(this.addCustomRessources(ressources));
		FXMLUtils.SHARED_FXML_LOADER.setBuilderFactory(new JavaFXBuilderFactory());
		final Region rv = FXMLUtils.SHARED_FXML_LOADER.load(this.inStream);
		this.inStream.close();
		this.controller = FXMLUtils.SHARED_FXML_LOADER.getController();
		if (this.controller != null) {
			assert FXMLUtils.assertInjection(this);
		} else {
			System.err.println("No controller loaded!, is this as expected?");
		}
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
	}

}
