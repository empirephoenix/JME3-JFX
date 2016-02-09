package com.jme3x.jfx;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3x.jfx.cursor.proton.ProtonCursorProvider;
import com.jme3x.jfx.dnd.DragLabel;
import com.jme3x.jfx.window.AbstractWindow;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;

public class TestDragDrop extends SimpleApplication {
	private static boolean	assertionsEnabled;
	public static Label		target;

	public static void main(final String[] args) {
		System.out.println("testd&d");
		assert TestDragDrop.enabled();
		if (!TestDragDrop.assertionsEnabled) {
			throw new RuntimeException("Assertions must be enabled (vm args -ea");
		}
		new TestDragDrop().start();
	}

	private static boolean enabled() {
		TestDragDrop.assertionsEnabled = true;
		return true;
	}

	@Override
	public void simpleInitApp() {
		this.setPauseOnLostFocus(false);
		this.flyCam.setDragToRotate(true);
		this.viewPort.setBackgroundColor(ColorRGBA.Red);

		final GuiManager testguiManager = new GuiManager(this.guiNode, this.assetManager, this, false, new ProtonCursorProvider(this, this.assetManager, this.inputManager));
		/**
		 * 2d gui, use the default input provider
		 */
		this.inputManager.addRawInputListener(testguiManager.getInputRedirector());

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				final AbstractWindow targetwindow = new AbstractWindow() {

					@Override
					protected Region innerInit() throws Exception {
						TestDragDrop.target = new Label("Drag target");

						TestDragDrop.target.setOnDragEntered(new EventHandler<DragEvent>() {
							@Override
							public void handle(final DragEvent event) {
								TestDragDrop.target.setText("DragEnter");
							}
						});

						TestDragDrop.target.setOnDragExited(new EventHandler<DragEvent>() {
							@Override
							public void handle(final DragEvent event) {
								TestDragDrop.target.setText("DrgExit");
							}
						});

						TestDragDrop.target.setOnDragOver(new EventHandler<DragEvent>() {
							@Override
							public void handle(final DragEvent event) {
								event.acceptTransferModes(TransferMode.COPY);
							}
						});

						TestDragDrop.target.setOnDragDropped(new EventHandler<DragEvent>() {

							@Override
							public void handle(final DragEvent event) {
								System.out.println("Dropped " + event.getGestureSource());
							}
						});
						return TestDragDrop.target;
					}

					@Override
					protected void afterInit() {
						this.setSize(300, 200);
					}

				};

				final AbstractWindow sourceWindow = new AbstractWindow() {

					@Override
					protected Region innerInit() throws Exception {
						final Label target = new DragLabel("Drag source");
						target.setOnDragDetected(new EventHandler<MouseEvent>() {

							@Override
							public void handle(final MouseEvent event) {
								// marker interface for event handling
							}
						});
						return target;
					}

					@Override
					protected void afterInit() {
						this.setSize(300, 200);
						this.setLayoutX(310);
					}
				};

				testguiManager.attachHudAsync(targetwindow);
				testguiManager.attachHudAsync(sourceWindow);
			}
		});
	}
}
