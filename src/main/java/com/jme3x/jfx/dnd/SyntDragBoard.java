package com.jme3x.jfx.dnd;

import java.util.HashMap;
import java.util.Map;

import com.jme3x.jfx.GuiManager;

import javafx.scene.Node;

public class SyntDragBoard {
	private Map<String, Object>	dataTransfer	= new HashMap<>();
	private Node				dragProxy;

	public void setDragProxy(final Node dragProxy) {
		dragProxy.setMouseTransparent(true);
		dragProxy.setStyle(GuiManager.DRAGPROXY_SPECIAL_TOFRONT); // marker layout
		dragProxy.setVisible(true);

		this.dragProxy = dragProxy;
	}

	public Map<String, Object> getDataTransfer() {
		return this.dataTransfer;
	}

	public boolean hasDragProxy() {
		return this.dragProxy != null;
	}

	public Node getDragProxy() {
		return this.dragProxy;
	}

}
