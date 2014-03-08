package com.jme3x.jfx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
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
	public GuiManager(final Node guiParent, final AssetManager assetManager, final Application application,
			final boolean fullscreen, final ICursorDisplayProvider cursorDisplayProvider) {

		this.jmefx = JmeFxContainer.install(application, guiParent, fullscreen, cursorDisplayProvider);
		guiParent.attachChild(this.jmefx.getJmeNode());

		if ( cursorDisplayProvider != null ) {
    		for (final CursorType type : CursorType.values()) {
    			cursorDisplayProvider.setup(type);
    		}
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

				// ensure that on every focues change between windows/huds modality is preserved!

				GuiManager.this.highLevelGroup.getChildren().addListener(new ListChangeListener<Object>() {
					boolean	ignoreEvents	= false;

					@Override
					public void onChanged(final Change<?> c) {
						// ensure it is not triggerd by the events it produces
						if (this.ignoreEvents) {
							return;
						}
						this.ignoreEvents = true;
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								GuiManager.this.sortWindowsBeforeHudsAndEnforceModality();
								ignoreEvents = false;
							}
						});
					}
				});

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
	 * removes a hud, if this is not called in the jfx thread this is done async, else it is done instantly
	 * 
	 * @param hud
	 */
	public void detachHudAsync(final AbstractHud hud) {
		final Runnable attachTask = new Runnable() {
			@Override
			public void run() {
				if (!hud.isAttached()) {
					return;
				}
				System.out.println("Detaching " + hud);
				GuiManager.this.attachedHuds.remove(hud);
				GuiManager.this.highLevelGroup.getChildren().remove(hud.getNode());
				hud.setAttached(false, null);
			}
		};
		FxPlatformExecutor.runOnFxApplication(attachTask);
	}

	/**
	 * adds a hud, if this is not called in the jfx thread this is done async, else it is done instantly
	 * 
	 * @param hud
	 */
	public void attachHudAsync(final AbstractHud hud) {
		final Runnable attachTask = new Runnable() {
			@Override
			public void run() {
				if (hud.isAttached()) {
					return;
				}
				System.out.println("Attaching " + hud);
				assert !GuiManager.this.attachedHuds.contains(hud) : "Duplicated attach of " + hud
						+ " isAttached state error?";
				if (!hud.isInitialized()) {
					System.err.println("Late init of " + hud.getClass().getName()
							+ " call initialize early to prevent microlags");
					hud.precache();
				}
				GuiManager.this.attachedHuds.add(hud);
				GuiManager.this.highLevelGroup.getChildren().add(hud.getNode());
				hud.setAttached(true, GuiManager.this);
			}
		};
		FxPlatformExecutor.runOnFxApplication(attachTask);
	}

	/**
	 * expected bahaviour, if a window is attached, move it to front <br>
	 * if a hud is attached move it behind all windows, but before already existing huds <br>
	 * dont change order of windows or order of huds.
	 **/
	private void sortWindowsBeforeHudsAndEnforceModality() {
		// TODO efficiency

		final ObservableList<javafx.scene.Node> currentOrder = this.highLevelGroup.getChildren();

		// read current order and split by windows and huds
		final ArrayList<AbstractWindow> orderedWindows = new ArrayList<>();
		final ArrayList<AbstractWindow> orderedModalWindows = new ArrayList<>();
		final ArrayList<AbstractHud> orderdHuds = new ArrayList<>();
		boolean switchToModal = false;
		for (final javafx.scene.Node n : currentOrder) {
			for (final AbstractHud hud : this.attachedHuds) {
				if (hud.getNode() == n) {
					if (hud instanceof AbstractWindow) {
						final AbstractWindow casted = (AbstractWindow) hud;
						if (casted.isModal()) {
							if (currentOrder.get(0) == casted.getNode()) {
								switchToModal = true;
							}
							orderedModalWindows.add((AbstractWindow) hud);
						} else {
							orderedWindows.add((AbstractWindow) hud);
						}
					} else {
						orderdHuds.add(hud);
					}
				}
			}
		}

		// clean current list, add huds first then windows

		currentOrder.clear();
		for (final AbstractHud hud : orderdHuds) {
			// disable them if a modal window exist(till a better solution is found for input interception)
			hud.getNode().disableProperty().set(orderedModalWindows.size() > 0);
			currentOrder.add(hud.getNode());
		}
		for (final AbstractWindow window : orderedWindows) {
			// disable them if a modal window exist(till a better solution is found for input interception)
			window.getNode().disableProperty().set(orderedModalWindows.size() > 0);
			currentOrder.add(window.getNode());
		}
		for (final AbstractWindow modalWindow : orderedModalWindows) {
			currentOrder.add(modalWindow.getNode());
			modalWindow.getNode().requestFocus();
		}
		if (!switchToModal && orderedModalWindows.size() > 0) {
			// TODO focuse notification
			System.out.println("TODO FocusDenied sound/visual representation");
		}
	}

	public List<AbstractHud> getAttachedHuds() {
		return Collections.unmodifiableList(this.attachedHuds);
	}

	/**
	 * this inputlistener recives all! events, even those that are normally consumed by JFX. <br>
	 * Usecase
	 * 
	 * @param rawInputListenerAdapter
	 */
	public void setEverListeningRawInputListener(final RawInputListener rawInputListenerAdapter) {
		this.jmefx.setEverListeningRawInputListener(rawInputListenerAdapter);
	}
}
