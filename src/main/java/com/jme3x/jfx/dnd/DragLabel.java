package com.jme3x.jfx.dnd;

import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * A label that will provide a similar looking dragproxy
 */
public class DragLabel extends Label implements DragProxyProvider {
	public DragLabel() {
		super();
	}

	public DragLabel(final String text, final Node graphic) {
		super(text, graphic);
	}

	public DragLabel(final String text) {
		super(text);
	}

	@Override
	public Node provide() {
		final Label lbl = new Label(this.getText());
		lbl.setStyle(this.getStyle());
		lbl.minHeight(this.getMinHeight());
		lbl.maxHeight(this.getMaxHeight());
		lbl.prefHeight(this.getPrefHeight());
		lbl.minWidth(this.getMinWidth());
		lbl.maxWidth(this.getMaxWidth());
		lbl.prefWidth(this.getPrefWidth());
		return lbl;
	}

}
