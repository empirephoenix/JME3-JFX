package com.jme3x.jfx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import jfxtras.scene.control.window.CloseIcon;

/**
 * redirects the close events to make it compatible with the GuiManager
 * 
 * @author empire
 * 
 */
public class AdjustedCloseIcon extends CloseIcon {

	public AdjustedCloseIcon(final AbstractWindow abstractWindow) {
		super(abstractWindow.getInnerWindow());

		this.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent t) {
				abstractWindow.close();
			}
		});
	}

}
