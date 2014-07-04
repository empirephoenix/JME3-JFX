package com.jme3x.jfx;

import java.nio.ByteBuffer;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
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
	private ImageView					dragImage;

	public JmeFxDNDHandler(final JmeFxContainer jmeFxContainer) {
		this.jmeFxContainer = jmeFxContainer;
	}

	@Override
	public void dragStarted(final EmbeddedSceneDSInterface dragSource, final TransferMode dragAction) {
		try {
			Object dimg = dragSource.getData("application/x-java-drag-image");
			if (dimg != null) {
				createDragImageProxy(dimg);
			}

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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * this is kinda ridiculous, but well at least it seems to work
	 * 
	 * @param jmeJfxDragImage
	 */
	private void createDragImageProxy(Object jmeJfxDragImage) {
		if (jmeJfxDragImage instanceof ByteBuffer) {
			try {
				ByteBuffer casted = (ByteBuffer) jmeJfxDragImage;
				casted.position(0);
				int w = casted.getInt();
				int h = casted.getInt();

				byte[] imgdata = new byte[casted.remaining()];
				casted.get(imgdata);

				WritableImage img = new WritableImage(w, h);
				PixelWriter writer = img.getPixelWriter();

				writer.setPixels(0, 0, w, h, WritablePixelFormat.getByteBgraInstance(), imgdata, 0, w * 4);
				// writer.setPixels(0, 0, w, h, WritablePixelFormat.getByteBgraInstance(), imgData, 0);

				dragImage = new ImageView(img);
				dragImage.setStyle("dragimage:true;");
				dragImage.setMouseTransparent(true);
				dragImage.setVisible(true);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// restore the hacky serialized dragimage

	}

	public void mouseUpdate(final int x, final int y, final boolean mousePressed) {
		try {
			if (this.dragSource == null || this.dropTarget == null) {
				return;
			}
			if (mousePressed) {
				if (dragImage != null) {
					dragImage.relocate(x, y);
					//only add once it has a valid position
					if (!jmeFxContainer.getRootNode().getChildren().contains(dragImage)) {
						jmeFxContainer.getRootNode().getChildren().add(dragImage);
					}
				}
				this.overtarget = this.dropTarget.handleDragOver(x, y, x, y, TransferMode.COPY);
			} else {
				if (dragImage != null) {
					jmeFxContainer.getRootNode().getChildren().remove(dragImage);
					dragImage = null;
				}
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
