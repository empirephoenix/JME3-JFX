package com.jme3x.jfx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import jfxtras.labs.scene.control.window.SelectableNode;
import jfxtras.labs.scene.control.window.Window;
import jfxtras.labs.scene.control.window.WindowIcon;
import jfxtras.labs.util.WindowUtil;

/**
 * extends the default minimize button with a few hacks to ignore minSize values when minimizing
 * 
 * @author empire
 * 
 */
public class AdjustedMinimizeIcon extends WindowIcon {

	public static final String	DEFAULT_STYLE_CLASS	= "window-minimize-icon";

	private final Window		w;

	public AdjustedMinimizeIcon(final Window w) {

		this.w = w;

		this.getStyleClass().setAll(AdjustedMinimizeIcon.DEFAULT_STYLE_CLASS);

		this.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent t) {
				// allow proper minimizing by unbinding this values, a listener in Abstract window will restore them
				w.setMinimized(!w.isMinimized());
				if (w.isMinimized()) {
					w.getContentPane().getChildren().clear();
					w.maxWidthProperty().unbind();
					w.maxHeightProperty().unbind();
					w.minHeightProperty().unbind();
					w.minWidthProperty().unbind();
					w.minHeightProperty().set(0);
					w.minHeightProperty().set(0);
				}

				if (w.isSelected()) {
					AdjustedMinimizeIcon.this.minimizeSelectedWindows();
				}
			}
		});
	}

	// TODO move from skin to behavior class (a lot of other stuff here too)
	private void minimizeSelectedWindows() {
		for (final SelectableNode sN : WindowUtil.getDefaultClipboard().getSelectedItems()) {

			if (sN == this.w || !(sN instanceof Window)) {
				continue;
			}

			final Window selectedWindow = (Window) sN;

			if (this.w.getParent().equals(selectedWindow.getParent())) {

				selectedWindow.setMinimized(!selectedWindow.isMinimized());
			}
		} // end for sN
	}
}
