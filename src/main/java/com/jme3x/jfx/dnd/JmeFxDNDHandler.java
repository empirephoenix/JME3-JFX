package com.jme3x.jfx.dnd;

import java.util.HashSet;
import java.util.function.Function;

import com.jme3x.jfx.GuiManager;
import com.jme3x.jfx.JmeFxContainer;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;

/**
 * Synthetic D&D manager to circumvent large amount of internal unreliable api. Reuses default eventhandlers where possible
 */
public class JmeFxDNDHandler {
	private HashSet<Node>				entered	= new HashSet<>();

	private JmeFxContainer				jmeFxContainer;

	private Node						dragging;

	private Function<Exception, Void>	exceptionHandler;

	private Node						dragProxy;

	public JmeFxDNDHandler(final JmeFxContainer jmeFxContainer) {
		this.jmeFxContainer = jmeFxContainer;
	}

	private void exception(final Exception e) {
		if (this.exceptionHandler != null) {
			this.exceptionHandler.apply(e);
		} else {
			e.printStackTrace();
		}
	}

	public void setExceptionHandler(final Function<Exception, Void> exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	public void mouseUpdate(final int x, final int y, final boolean mousePressed) {
		if (mousePressed) {
			if (this.dragging == null) {
				final Node dragElement = this.getDragSourceAt(this.jmeFxContainer.getRootNode(), x, y);
				if (dragElement != null) {
					try {
						dragElement.getOnDragDetected().handle(null);
					} catch (final Exception e) {
						this.exception(e);
					}
					if (dragElement instanceof DragProxyProvider) {
						final DragProxyProvider provider = (DragProxyProvider) dragElement;
						this.dragProxy = provider.provide();
					} else {
						this.dragProxy = new Label("X");
						this.dragProxy.minHeight(64);
						this.dragProxy.minWidth(64);
					}
					System.out.println("Started dragging");
					this.dragProxy.setMouseTransparent(true);
					this.dragProxy.setStyle(GuiManager.DRAGPROXY_SPECIAL_TOFRONT); // marker layout
					this.dragProxy.setVisible(true);

					this.jmeFxContainer.getRootChildren().add(this.dragProxy);
					this.dragging = dragElement;
				}
			}
		} else {
			if (this.dragging != null) {
				this.jmeFxContainer.getRootChildren().remove(this.dragProxy);

				for (final Node enter : this.entered) {
					if (enter.onDragExitedProperty() != null) {
						final DragEvent event = new DragEvent(DragEvent.DRAG_EXITED_TARGET, null, x, y, x, x, TransferMode.COPY, null, null, null);
						try {
							enter.getOnDragExited().handle(event);
						} catch (final Exception e) {
							this.exception(e);
						}
					}
				}
				final Node dropTarget = this.getDropTargetAt(this.jmeFxContainer.getRootNode(), x, y);
				if (dropTarget != null) {
					final DragEvent event = new DragEvent(DragEvent.DRAG_DROPPED, null, x, y, x, x, TransferMode.COPY, this.dragging, null, null);
					try {
						dropTarget.getOnDragDropped().handle(event);
					} catch (final Exception e) {
						this.exception(e);
					}
				}
				this.dragging = null;
			}
		}
		if (this.dragging != null) {
			if (this.dragProxy != null) {
				this.dragProxy.setLayoutX(x);
				this.dragProxy.setLayoutY(y);
				this.dragProxy.toFront();
			}
			this.processDragEvents(this.jmeFxContainer.getRootNode(), x, y);
		}
	}

	private Node processDragEvents(final Node current, final int x, final int y) {
		final Bounds bounds = current.localToScene(current.getBoundsInLocal());
		if (bounds.contains(x, y)) {
			if (current.getOnDragEntered() != null && !this.entered.contains(current)) {
				this.entered.add(current);
				final DragEvent event = new DragEvent(DragEvent.DRAG_ENTERED_TARGET, null, x, y, x, x, TransferMode.COPY, null, null, null);
				try {
					current.getOnDragEntered().handle(event);
				} catch (final Exception e) {
					this.exception(e);
				}
			}
		} else {
			if (this.entered.contains(current) && current.getOnDragExited() != null) {
				this.entered.remove(current);
				final DragEvent event = new DragEvent(DragEvent.DRAG_EXITED_TARGET, null, x, y, x, x, TransferMode.COPY, null, null, null);
				try {
					current.getOnDragExited().handle(event);
				} catch (final Exception e) {
					this.exception(e);
				}
			}
		}

		if (current instanceof Parent) {
			final Parent p = (Parent) current;
			for (final Node child : p.getChildrenUnmodifiable()) {
				this.processDragEvents(child, x, y);
			}
		}

		return null;
	}

	private Node getDragSourceAt(final Node current, final int x, final int y) {
		if (current.getOnDragDetected() != null) {
			return current;
		}

		if (current instanceof Parent) {
			final Parent p = (Parent) current;
			for (final Node child : p.getChildrenUnmodifiable()) {
				final Bounds bounds = child.localToScene(child.getBoundsInLocal());
				if (bounds.contains(x, y)) {
					final Node found = this.getDragSourceAt(child, x, y);
					if (found != null) {
						return found;
					}
				}

			}
		}

		return null;
	}

	private Node getDropTargetAt(final Node current, final int x, final int y) {
		if (current.getOnDragDropped() != null) {
			return current;
		}

		if (current instanceof Parent) {
			final Parent p = (Parent) current;
			for (final Node child : p.getChildrenUnmodifiable()) {
				final Bounds bounds = child.localToScene(child.getBoundsInLocal());
				if (bounds.contains(x, y)) {
					final Node found = this.getDropTargetAt(child, x, y);
					if (found != null) {
						return found;
					}
				}

			}
		}

		return null;
	}
}
