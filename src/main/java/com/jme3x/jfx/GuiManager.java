package com.jme3x.jfx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.input.RawInputListener;
import com.jme3.scene.Node;
import com.jme3x.jfx.cursor.ICursorDisplayProvider;
import com.sun.javafx.cursor.CursorType;

public class GuiManager {

	private JmeFxContainer		jmefx;
	private Group				highLevelGroup;
	private Scene				mainScene;
	/**
	 * a list of all attached huds, using copyonwrite to allow reading from other threads in a save way
	 */
	private List<AbstractHud>	attachedHuds	= new CopyOnWriteArrayList<>();

	public Group getRootGroup() {
		return this.highLevelGroup;
	}

	public JmeFxContainer getjmeFXContainer() {
		return this.jmefx;

	}

	/**
	 * creates a new JMEFX container, this is a rather expensive operation and should only be done one time fr the 2d fullscreengui. Additionals should only be necessary for 3d guis, should be called from JME thread
	 * 
	 * @param guiParent
	 * @param assetManager
	 * @param application
	 * @param fullscreen
	 */
	public GuiManager(final Node guiParent, final AssetManager assetManager, final Application application, final boolean fullscreen, final ICursorDisplayProvider cursorDisplayProvider) {

		this.jmefx = JmeFxContainer.install(application, guiParent, fullscreen, cursorDisplayProvider);
		guiParent.attachChild(this.jmefx.getJmeNode());

		for (final CursorType type : CursorType.values()) {
			cursorDisplayProvider.setup(type);
		}
		this.initRootGroup();

	}

	private void initRootGroup() {
		/*
		 * 
		 * Group baseHighLevelGroup = new Group(); Scene baseScene = new Scene(baseHighLevelGroup); baseScene.setFill(new Color(0, 0, 0, 0)); switchRootGroup(baseHighLevelGroup); }
		 * 
		 * private void switchRootGroup(Group newRootGroup) {
		 */
		final Semaphore waitForInit = new Semaphore(0);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				GuiManager.this.highLevelGroup = new Group();
				GuiManager.this.mainScene = new Scene(GuiManager.this.highLevelGroup);
				GuiManager.this.mainScene.setFill(new Color(0, 0, 0, 0));
				GuiManager.this.jmefx.setScene(GuiManager.this.mainScene);
				waitForInit.release();
			}
		});
		waitForInit.acquireUninterruptibly();
	}

	/**
	 * bind your input suppliery here, for 2d the normal inputmanager will suffice, Events are expected to be in the JME thread
	 * 
	 * @return
	 */
	public RawInputListener getInputRedirector() {
		return this.jmefx.inputListener;
	}

	/**
	 * adds a hud, if this is not called in the jfx thread this is done async, else it is done instantly
	 * 
	 * @param hud
	 */
	public void attachHudAsync(final AbstractHud hud) {
		if (!hud.isInitialized()) {
			System.err.println("Late init of " + hud.getClass().getName() + " call initialize early to prevent microlags");
			hud.precache();
			// TODO logger
		}

		final Runnable attachTask = new Runnable() {
			@Override
			public void run() {
				GuiManager.this.attachedHuds.add(hud);
				GuiManager.this.highLevelGroup.getChildren().add(hud.getNode());

				GuiManager.this.sortWindowsBeforeHuds(GuiManager.this.attachedHuds, GuiManager.this.highLevelGroup.getChildren());
			}
		};
		FxPlatformExecutor.runOnFxApplication(attachTask);
	}

	/**
	 * expected bahaviour, if a window is attached, move it to front <br>
	 * if a hud is attached move it behind all windows, but before already existing huds <br>
	 * dont change order of windows or order of huds.
	 **/
	private void sortWindowsBeforeHuds(final List<AbstractHud> attachedHuds, final ObservableList<javafx.scene.Node> currentOrder) {
		// TODO efficiency

		// read current order and split by windows and huds
		final ArrayList<AbstractWindow> orderedWindows = new ArrayList<>();
		final ArrayList<AbstractHud> orderdHuds = new ArrayList<>();
		for (final javafx.scene.Node n : currentOrder) {
			for (final AbstractHud hud : attachedHuds) {
				if (hud.getNode() == n) {
					if (hud instanceof AbstractWindow) {
						orderedWindows.add((AbstractWindow) hud);
					} else {
						orderdHuds.add(hud);
					}
				}
			}
		}

		// clean current list, add huds first then windows
		currentOrder.clear();
		for (final AbstractHud hud : orderdHuds) {
			currentOrder.add(hud.getNode());
		}
		for (final AbstractWindow window : orderedWindows) {
			currentOrder.add(window.getNode());
		}
	}

	public List<AbstractHud> getAttachedHuds() {
		return Collections.unmodifiableList(this.attachedHuds);
	}

	public void detachHudAsync(final AbstractHud hud) {
		if (Platform.isFxApplicationThread()) {
			GuiManager.this.highLevelGroup.getChildren().remove(hud.getNode());
			this.attachedHuds.remove(hud);
		} else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					GuiManager.this.attachedHuds.remove(hud);
					GuiManager.this.highLevelGroup.getChildren().remove(hud.getNode());
				}
			});
		}
	}
}
