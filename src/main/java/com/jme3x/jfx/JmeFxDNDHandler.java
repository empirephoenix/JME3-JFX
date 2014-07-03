package com.jme3x.jfx;

import javafx.scene.input.Clipboard;
import javafx.scene.input.TransferMode;

import com.sun.javafx.embed.EmbeddedSceneDSInterface;
import com.sun.javafx.embed.EmbeddedSceneDTInterface;
import com.sun.javafx.embed.HostDragStartListener;

/**
 * A very hacky implementation of a DND system, similar to SwingDND but for jme context. <br>
 * Allows for inner application drag and drop support. <br>
 * Cross GuiManager support is untested.
 * 
 * @author empire
 *
 */
public class JmeFxDNDHandler implements HostDragStartListener {
	private JmeFxContainer				jmeFxContainer;
	private EmbeddedSceneDTInterface	dropTarget;
	// mouse event stuff
	private EmbeddedSceneDSInterface	dragSource;
	private TransferMode				overtarget;

	public JmeFxDNDHandler(final JmeFxContainer jmeFxContainer) {
		this.jmeFxContainer = jmeFxContainer;
	}

	@Override
	public void dragStarted(final EmbeddedSceneDSInterface dragSource, final TransferMode dragAction) {
		this.jmeFxContainer.getInputListener().setMouseDNDListener(this);
		assert dragAction == TransferMode.COPY : "Only Copy is supported currently";
		System.out.println("Drag started of " + dragSource + " in mode " + dragAction);
		final Clipboard clip = Clipboard.getSystemClipboard();
		System.out.println(clip);

		assert this.dragSource == null;
		assert this.dropTarget == null;

		this.dragSource = dragSource;
		this.dropTarget = JmeFxDNDHandler.this.jmeFxContainer.scenePeer.createDropTarget();
		// pseudo enter, we only support inner events, so it stays always entered
		this.dropTarget.handleDragEnter(0, 0, 0, 0, TransferMode.COPY, dragSource);
	}

	public void mouseUpdate(final int x, final int y, final boolean mousePressed) {
		if (this.dragSource == null || this.dropTarget == null) {
			return;
		}
		System.out.println(x + " " + y + " " + mousePressed);
		if (mousePressed) {
			this.overtarget = this.dropTarget.handleDragOver(x, y, x, y, TransferMode.COPY);
		} else {
			System.out.println("Drag released!");
			if (this.overtarget != null) {
				// // causes exceptions when done without a target
				this.overtarget = JmeFxDNDHandler.this.dropTarget.handleDragOver(x, y, x, y, TransferMode.COPY);
				final TransferMode acceptedMode = JmeFxDNDHandler.this.dropTarget.handleDragDrop(x, y, x, y, TransferMode.COPY);
				// // Necessary to reset final the internal states, and allow final another drag drop
				this.dragSource.dragDropEnd(acceptedMode);
			} else {
				System.out.println("invalid drag target");
				// // seems to be necessary if no dragdrop attempt is being made
				JmeFxDNDHandler.this.dropTarget.handleDragLeave();
				this.dragSource.dragDropEnd(null);
			}
			this.jmeFxContainer.getInputListener().setMouseDNDListener(null);
			this.dragSource = null;
			this.dropTarget = null;
		}
	}
}
