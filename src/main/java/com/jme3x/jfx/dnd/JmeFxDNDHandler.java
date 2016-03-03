package com.jme3x.jfx.dnd;

import java.util.HashSet;
import java.util.function.Function;

import com.jme3x.jfx.JmeFxContainer;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * Synthetic D&D manager to circumvent large amount of internal unreliable api. Reuses default eventhandlers where possible
 */
public class JmeFxDNDHandler {
	private HashSet<Node>				entered			= new HashSet<>();

	private JmeFxContainer				jmeFxContainer;

	private Function<Exception, Void>	exceptionHandler;

	SyntDragBoard						dragAndDrop;

	private Node						dragging;

	private int							lasty;

	private int							lastx;

	private int							DRAG_TRIGGER	= 1;

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

	public void toFront(final Node target) {
		if (target.getParent() != null) {
			final Parent parent = target.getParent();
			if (parent.getParent() == null) {
				// if this is the first child under the rootnode
				target.toFront();
				return;
			}
			this.toFront(parent);
		}
	}

	public void mouseUpdate(final int x, final int y, final int sx, final int sy, final boolean mousePressed) {
		if (mousePressed) {
			if (this.lastx == -1) {
				this.lastx = x;
				this.lasty = y;
			}
			if (Math.abs(x - this.lastx) < this.DRAG_TRIGGER && Math.abs(y - this.lasty) < this.DRAG_TRIGGER) {
				return;
			}

			if (this.dragAndDrop == null && this.dragging == null) {
				final Node dragElement = this.getDragSourceAt(this.jmeFxContainer.getRootNode(), x, y);
				if (dragElement != null) {
					this.toFront(dragElement);
					if (dragElement.getOnMouseDragged() != null) {
						this.dragging = dragElement;
					} else if (dragElement.getOnDragDetected() != null) {
						dragElement.screenToLocal(sx, sy);
						final Point2D local = dragElement.sceneToLocal(x, y);

						this.startDragAndDrop(local.getX(), local.getY(), dragElement, sx, sy);
					}
					// allow one pulse for other event processing
					return;
				}
			}
		} else {
			this.lastx = -1;
			this.lasty = -1;
			if (this.dragging != null) {
				this.dragging = null;
			}
			if (this.dragAndDrop != null) {
				this.drop(x, y);
			}
		}
		if (this.dragging != null) {
			final Point2D local = this.dragging.sceneToLocal(x, y);
			this.dragging.getOnMouseDragged().handle(new MouseEvent(this.dragging, null, null, local.getX(), local.getY(), sx, sy, MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, false, false, false, null));
		}
		if (this.dragAndDrop != null) {
			this.updateDragAndDrop(x, y, sx, sy);
		}
	}

	private void updateDragAndDrop(final int x, final int y, final int sx, final int sy) {
		if (this.dragAndDrop.isAbort()) {
			this.lastx = -1;
			this.lasty = -1;
			this.dragAndDrop = null;
		}
		final Node dragProxy = this.dragAndDrop.getDragProxy();
		if (dragProxy != null) {
			dragProxy.setLayoutX(x);
			dragProxy.setLayoutY(y);
			dragProxy.toFront();
		}
		this.processDragEvents(this.jmeFxContainer.getRootNode(), x, y, sx, sy);
	}

	private void drop(final int x, final int y) {
		this.jmeFxContainer.getRootChildren().remove(this.dragAndDrop.getDragProxy());

		for (final Node enter : this.entered) {
			if (enter.onDragExitedProperty() != null) {
				final DragEvent event = new DragEvent(this.dragAndDrop, null, DragEvent.DRAG_EXITED_TARGET, null, x, y, x, x, TransferMode.COPY, null, null, null);
				try {
					enter.getOnDragExited().handle(event);
				} catch (final Exception e) {
					this.exception(e);
				}
			}
		}
		final Node dropTarget = this.getDropTargetAt(this.jmeFxContainer.getRootNode(), x, y);
		if (dropTarget != null) {
			this.dragAndDrop.getDataTransfer().put("targetElement", dropTarget);
			final DragEvent event = new DragEvent(this.dragAndDrop, null, DragEvent.DRAG_DROPPED, null, x, y, x, x, TransferMode.COPY, null, null, null);
			try {
				dropTarget.getOnDragDropped().handle(event);
			} catch (final Exception e) {
				this.exception(e);
			}
		}
		this.dragAndDrop = null;
	}

	private void startDragAndDrop(final double d, final double f, final Node dragElement, final double sx, final double sy) {
		final SyntDragBoard dragAndDrop = new SyntDragBoard();
		dragAndDrop.getDataTransfer().put("sourceElement", dragElement);
		try {
			final MouseDragEvent pseudoDrag = new MouseDragEvent(dragAndDrop, null, null, d, f, sx, sy, MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, false, false, null, null);
			dragElement.getOnDragDetected().handle(pseudoDrag);
		} catch (final Exception e) {
			this.exception(e);
		}
		if (!dragAndDrop.hasDragProxy()) {
			final Node dragProxy = new Label("X");
			dragProxy.minHeight(64);
			dragProxy.minWidth(64);
			dragAndDrop.setDragProxy(dragProxy);
		}
		if (dragAndDrop.isAbort()) {
			this.lastx = -1;
			this.lasty = -1;
			return;
		} else {
			this.dragAndDrop = dragAndDrop;
			this.jmeFxContainer.getRootChildren().add(this.dragAndDrop.getDragProxy());
		}
	}

	private Node processDragEvents(final Node current, final int x, final int y, final int sx, final int sy) {
		final Bounds bounds = current.localToScene(current.getBoundsInLocal());
		if (bounds.contains(x, y)) {
			if (current.getOnDragEntered() != null && !this.entered.contains(current)) {
				this.entered.add(current);
				final DragEvent event = new DragEvent(this.dragAndDrop, null, DragEvent.DRAG_ENTERED_TARGET, null, x, y, sx, sy, TransferMode.COPY, null, null, null);
				try {
					current.getOnDragEntered().handle(event);
				} catch (final Exception e) {
					this.exception(e);
				}
			}
		} else {
			if (this.entered.contains(current) && current.getOnDragExited() != null) {
				this.entered.remove(current);
				final DragEvent event = new DragEvent(this.dragAndDrop, null, DragEvent.DRAG_EXITED_TARGET, null, x, y, sx, sy, TransferMode.COPY, null, null, null);
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
				this.processDragEvents(child, x, y, sx, sy);
			}
		}

		return null;
	}

	private Node getDragSourceAt(final Node current, final int x, final int y) {
		if (current.getOnMouseDragged() != null) {
			return current;
		}
		if (current.getOnDragDetected() != null) {
			return current;
		}

		if (current instanceof Parent) {
			final Parent p = (Parent) current;
			for (int i = p.getChildrenUnmodifiable().size() - 1; i >= 0; i--) {
				final Node child = p.getChildrenUnmodifiable().get(i);
				final Bounds bounds = child.localToScene(child.getBoundsInLocal());
				if (bounds.contains(x, y)) {
					final Node found = this.getDragSourceAt(child, x, y);
					return found;
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
			for (int i = p.getChildrenUnmodifiable().size() - 1; i >= 0; i--) {
				final Node child = p.getChildrenUnmodifiable().get(i);
				final Bounds bounds = child.localToScene(child.getBoundsInLocal());
				if (bounds.contains(x, y)) {
					final Node found = this.getDropTargetAt(child, x, y);
					return found;
				}

			}
		}

		return null;
	}
}
