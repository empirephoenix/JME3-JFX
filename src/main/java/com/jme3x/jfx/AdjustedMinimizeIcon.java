package com.jme3x.jfx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import jfxtras.scene.control.window.MinimizeIcon;
import jfxtras.scene.control.window.Window;

/**
 * extends the default minimize button with a few hacks to ignore minSize values when minimizing
 * 
 * @author empire
 * 
 */
public class AdjustedMinimizeIcon extends MinimizeIcon {

	public static final String	DEFAULT_STYLE_CLASS	= "window-minimize-icon";

	private final Window		w;

	public AdjustedMinimizeIcon(final AbstractWindow abstractWindow) {
		super(abstractWindow.getInnerWindow());
		this.w = abstractWindow.getInnerWindow();
		this.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent t) {
				// allow proper minimizing by unbinding this values, a listener in Abstract window will restore them
				AdjustedMinimizeIcon.this.w.setMinimized(!AdjustedMinimizeIcon.this.w.isMinimized());
				if (AdjustedMinimizeIcon.this.w.isMinimized()) {
					AdjustedMinimizeIcon.this.w.getContentPane().getChildren().clear();
					AdjustedMinimizeIcon.this.w.maxWidthProperty().unbind();
					AdjustedMinimizeIcon.this.w.maxHeightProperty().unbind();
					AdjustedMinimizeIcon.this.w.minHeightProperty().unbind();
					AdjustedMinimizeIcon.this.w.minWidthProperty().unbind();
					AdjustedMinimizeIcon.this.w.minHeightProperty().set(0);
					AdjustedMinimizeIcon.this.w.minWidthProperty().set(0);
				}
			}
		});
	}
}
