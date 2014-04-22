package com.jme3x.jfx;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Camera;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import org.lwjgl.opengl.Display;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.asset.AssetManager;
import com.jme3.input.RawInputListener;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.jme3.util.BufferUtils;
import com.jme3x.jfx.cursor.ICursorDisplayProvider;
import com.sun.glass.ui.Pixels;
import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.cursor.CursorType;
import com.sun.javafx.embed.AbstractEvents;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.EmbeddedStageInterface;
import com.sun.javafx.embed.HostInterface;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.scene.SceneHelper.SceneAccessor;
import com.sun.javafx.stage.EmbeddedWindow;

/**
 * Need to pass -Dprism.dirtyopts=false on startup
 * 
 * @author abies / Artur Biesiadowski
 */
public class JmeFxContainer {

	EmbeddedStageInterface		stagePeer;
	EmbeddedSceneInterface		scenePeer;
	volatile EmbeddedWindow		stage;
	HostInterface				hostContainer;
	JmeFXInputListener			inputListener;
	int							pWidth;
	int							pHeight;
	volatile Scene				scene;
	Picture						picture;
	Image						jmeImage;
	Texture2D					tex;
	ByteBuffer					jmeData;
	ByteBuffer					fxData;
	boolean						fxDataReady		= false;
	int							oldX			= -1;
	int							oldY			= -1;
	boolean						focus;
	Application					app;
	boolean						fullScreenSuppport;
	CompletableFuture<Format>	nativeFormat	= new CompletableFuture<Format>();
	ICursorDisplayProvider		cursorDisplayProvider;

	public static JmeFxContainer install(final Application app, final Node guiNode, final boolean fullScreenSupport, final ICursorDisplayProvider cursorDisplayProvider) {
		final JmeFxContainer ctr = new JmeFxContainer(app.getAssetManager(), app, fullScreenSupport, cursorDisplayProvider);
		guiNode.attachChild(ctr.getJmeNode());
		ctr.inputListener = new JmeFXInputListener(ctr);
		app.getInputManager().addRawInputListener(ctr.inputListener);

		if (fullScreenSupport) {
			ctr.installSceneAccessorHack();
		}

		return ctr;
	}

	private JmeFxContainer(final AssetManager assetManager, final Application app, final boolean fullScreenSupport, final ICursorDisplayProvider cursorDisplayProvider) {

		this.cursorDisplayProvider = cursorDisplayProvider;
		this.app = app;
		this.fullScreenSuppport = fullScreenSupport;

		app.getStateManager().attach(new AbstractAppState() {
			@Override
			public void cleanup() {
				Platform.exit();
				super.cleanup();
			}
		});

		this.hostContainer = new JmeFXHostInterfaceImpl(this);
		this.picture = new Picture("JavaFXContainer", true) {
			@Override
			public void updateLogicalState(final float tpf) {
				if (JmeFxContainer.this.stagePeer != null) {

					if (Display.getWidth() != JmeFxContainer.this.pWidth || Display.getHeight() != JmeFxContainer.this.pHeight) {
						JmeFxContainer.this.handleResize();
					}

					final int x = Display.getX() + (Display.isFullscreen() ? 0 : 3);
					final int y = Display.getY() + (Display.isFullscreen() ? 0 : 25);
					if (JmeFxContainer.this.oldX != x || JmeFxContainer.this.oldY != y) {
						JmeFxContainer.this.oldX = x;
						JmeFxContainer.this.oldY = y;
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								JmeFxContainer.this.stagePeer.setLocation(x, y);
							}
						});
					}

				}
				super.updateLogicalState(tpf);
			}
		};

		this.picture.move(0, 0, -1);
		this.picture.setPosition(0, 0);

		this.initFx();

		this.handleResize();

		this.tex = new Texture2D(this.jmeImage);
		this.picture.setTexture(assetManager, this.tex, true);

	}

	private void handleResize() {

		try {
			this.imageExchange.acquire();

			this.pWidth = Display.getWidth();
			this.pHeight = Display.getHeight();
			if (this.pWidth < 64) {
				this.pWidth = 64;
			}
			if (this.pHeight < 64) {
				this.pHeight = 64;
			}
			this.picture.setWidth(this.pWidth);
			this.picture.setHeight(this.pHeight);
			this.jmeData = BufferUtils.createByteBuffer(this.pWidth * this.pHeight * 4);
			this.fxData = BufferUtils.createByteBuffer(this.pWidth * this.pHeight * 4);

			this.jmeImage = new Image(this.nativeFormat.get(), this.pWidth, this.pHeight, this.jmeData);
			if (this.tex != null) {
				this.tex.setImage(this.jmeImage);
			}

			if (this.stagePeer != null) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						JmeFxContainer.this.stagePeer.setSize(JmeFxContainer.this.pWidth, JmeFxContainer.this.pHeight);
						JmeFxContainer.this.scenePeer.setSize(JmeFxContainer.this.pWidth, JmeFxContainer.this.pHeight);
						JmeFxContainer.this.hostContainer.repaint();
					}
				});
			}

		} catch (final Exception exc) {
			exc.printStackTrace();
		} finally {
			this.imageExchange.release();
		}
	}

	public Picture getJmeNode() {
		return this.picture;
	}

	public int getWindowX() {
		return this.oldX;
	}

	public int getWindowY() {
		return this.oldY;
	}

	private void initFx() {
		PlatformImpl.startup(new Runnable() {
			@Override
			public void run() {
				switch (Pixels.getNativeFormat()) {
				case Pixels.Format.BYTE_ARGB:
					JmeFxContainer.this.nativeFormat.complete(Format.ARGB8);
					break;
				case Pixels.Format.BYTE_BGRA_PRE:
					JmeFxContainer.this.nativeFormat.complete(Format.BGRA8);
					break;
				default:
					// this is wrong, but at least will display something
					JmeFxContainer.this.nativeFormat.complete(Format.ARGB8);
					break;
				}
			}
		});

	}

	void setFxEnabled(final boolean enabled) {
	}

	public Scene getScene() {
		return this.scene;
	}

	public EmbeddedWindow getStage() {
		return this.stage;
	}

	public void setScene(final Scene newScene) {
		FxPlatformExecutor.runOnFxApplication(new Runnable() {
			@Override
			public void run() {
				JmeFxContainer.this.setSceneImpl(newScene);
			}
		});
	}

	/*
	 * Called on JavaFX app thread.
	 */
	private void setSceneImpl(final Scene newScene) {
		if (this.stage != null && newScene == null) {
			this.stage.hide();
			this.stage = null;
		}

		this.app.enqueue(new Callable<Void>() {
			@Override
			public Void call() {
				JmeFxContainer.this.picture.setCullHint(newScene == null ? CullHint.Always : CullHint.Never);
				return null;
			}
		});

		this.scene = newScene;
		if (this.stage == null && newScene != null) {
			this.stage = new EmbeddedWindow(this.hostContainer);
		}
		if (this.stage != null) {
			this.stage.setScene(newScene);
			if (!this.stage.isShowing()) {
				this.stage.show();
			}
		}
	}

	private Semaphore	imageExchange	= new Semaphore(1);
	public CursorType	lastcursor;

	void paintComponent() {
		if (this.scenePeer == null) {
			return;
		}

		final boolean lock = this.imageExchange.tryAcquire();
		if (!lock) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					JmeFxContainer.this.paintComponent();
				}
			});
			return;
		}

		try {

			final ByteBuffer data = this.fxData;
			data.clear();

			final IntBuffer buf = data.asIntBuffer();

			final long start = System.currentTimeMillis();
			if (!this.scenePeer.getPixels(buf, this.pWidth, this.pHeight)) {
				return;
			}

			if (this.fullScreenSuppport) {
				for (final PopupSnapper ps : this.activeSnappers) {
					ps.paint(buf, this.pWidth, this.pHeight);
				}
			}

			data.flip();
			this.fxDataReady = true;

		} catch (final Exception exc) {
			exc.printStackTrace();
		} finally {
			this.imageExchange.release();
		}
		this.app.enqueue(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final boolean updateImage = JmeFxContainer.this.imageExchange.tryAcquire();
				// we update only if we can do that in nonblocking mode
				// if would need to block, it means that another callable with newer data will be
				// enqueued soon, so we can just ignore this repaint
				if (updateImage) {
					try {
						if (JmeFxContainer.this.fxDataReady) {
							JmeFxContainer.this.fxDataReady = false;
							final ByteBuffer tmp = JmeFxContainer.this.jmeData;
							JmeFxContainer.this.jmeData = JmeFxContainer.this.fxData;
							JmeFxContainer.this.fxData = tmp;
						}
					} finally {
						JmeFxContainer.this.imageExchange.release();
					}
					JmeFxContainer.this.jmeImage.setData(JmeFxContainer.this.jmeData);
				} else {
					// System.out.println("Skipping update due to contention");
				}
				return null;
			}
		});

	}

	boolean[]	mouseButtonState	= new boolean[3];

	public boolean isCovered(final int x, final int y) {
		if (x < 0 || x >= this.pWidth) {
			return false;
		}
		if (y < 0 || y >= this.pHeight) {
			return false;
		}
		final ByteBuffer data = this.jmeImage.getData(0);
		data.limit(data.capacity());
		final int alpha = data.get(3 + 4 * (y * this.pWidth + x));
		data.limit(0);
		return alpha != 0;
	}

	public void grabFocus() {
		if (!this.focus && this.stagePeer != null) {
			this.stagePeer.setFocused(true, AbstractEvents.FOCUSEVENT_ACTIVATED);
			this.focus = true;
		}
	}

	public void loseFocus() {
		if (this.focus && this.stagePeer != null) {
			this.stagePeer.setFocused(false, AbstractEvents.FOCUSEVENT_DEACTIVATED);
			this.focus = false;
		}
	}

	private char[]	keyCharSet	= new char[0xFF];
	private BitSet	keyStateSet	= new BitSet(0xFF);

	int retrieveKeyState() {
		int embedModifiers = 0;

		if (this.keyStateSet.get(KeyEvent.VK_SHIFT)) {
			embedModifiers |= AbstractEvents.MODIFIER_SHIFT;
		}

		if (this.keyStateSet.get(KeyEvent.VK_CONTROL)) {
			embedModifiers |= AbstractEvents.MODIFIER_CONTROL;
		}

		if (this.keyStateSet.get(KeyEvent.VK_ALT)) {
			embedModifiers |= AbstractEvents.MODIFIER_ALT;
		}

		if (this.keyStateSet.get(KeyEvent.VK_META)) {
			embedModifiers |= AbstractEvents.MODIFIER_META;
		}
		return embedModifiers;
	}

	Map<Window, PopupSnapper>	snappers		= new IdentityHashMap<>();
	List<PopupSnapper>			activeSnappers	= new CopyOnWriteArrayList<>();

	private void installSceneAccessorHack() {

		try {
			final Field f = SceneHelper.class.getDeclaredField("sceneAccessor");
			f.setAccessible(true);
			final SceneAccessor orig = (SceneAccessor) f.get(null);

			final SceneAccessor sa = new SceneAccessor() {
				@Override
				public void setPaused(final boolean paused) {
					orig.setPaused(paused);
				}

				@Override
				public void parentEffectiveOrientationInvalidated(final Scene scene) {
					orig.parentEffectiveOrientationInvalidated(scene);
				}

				@Override
				public Camera getEffectiveCamera(final Scene scene) {
					return orig.getEffectiveCamera(scene);
				}

				@Override
				public Scene createPopupScene(final Parent root) {
					final Scene scene = orig.createPopupScene(root);
					scene.windowProperty().addListener(new ChangeListener<Window>() {
						@Override
						public void changed(final javafx.beans.value.ObservableValue<? extends Window> observable, final Window oldValue, final Window window) {
							window.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
								@Override
								public void handle(final WindowEvent event) {
									if (Display.isFullscreen()) {
										final PopupSnapper ps = new PopupSnapper(JmeFxContainer.this, window, scene);
										JmeFxContainer.this.snappers.put(window, ps);
										ps.start();
									}
								}
							});
						};
					});

					scene.windowProperty().addListener(new ChangeListener<Window>() {
						@Override
						public void changed(final javafx.beans.value.ObservableValue<? extends Window> observable, final Window oldValue, final Window window) {
							window.addEventHandler(WindowEvent.WINDOW_HIDDEN, new EventHandler<WindowEvent>() {
								@Override
								public void handle(final WindowEvent event) {
									if (Display.isFullscreen()) {
										final PopupSnapper ps = JmeFxContainer.this.snappers.remove(window);
										if (ps == null) {
											System.out.println("Cannot find snapper for window " + window);
										} else {
											ps.stop();
										}
									}
								}
							});
						};
					});

					return scene;
				}
			};

			f.set(null, sa);
		} catch (final Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * call via gui manager!
	 * 
	 * @param rawInputListenerAdapter
	 */
	public void setEverListeningRawInputListener(final RawInputListener rawInputListenerAdapter) {
		this.inputListener.setEverListeningRawInputListener(rawInputListenerAdapter);
	}
}