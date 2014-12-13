package com.jme3x.jfx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import jfxtras.scene.control.window.WindowIcon;

public class ExternalizeIcon extends WindowIcon {
	public static final String	DEFAULT_STYLE_CLASS	= "window-rotate-icon";

	public ExternalizeIcon(final AbstractWindow w) {
		this.getStyleClass().setAll(ExternalizeIcon.DEFAULT_STYLE_CLASS);
		this.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				w.setExternalized(true);
			}
		});
	}

}
