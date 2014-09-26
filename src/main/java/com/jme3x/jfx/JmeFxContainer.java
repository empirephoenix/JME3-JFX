package com.jme3x.jfx;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import java.util.function.Function;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.jme3x.jfx.util.JFXUtils;
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
	private static final Logger logger = LoggerFactory.getLogger(JmeFxContainer.class);

	EmbeddedStageInterface		stagePeer;
	EmbeddedSceneInterface		scenePeer;
	volatile EmbeddedWindow		stage;
	HostInterface				hostContainer;
	JmeFXInputListener			inputListener;
	int							pWidth;
	int							pHeight;
	volatile Scene				scene;
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
	private Group				rootNode;

	private final Picture		picture;
	private Function<JmeFxContainer, Void> exchangeData;

	/** Indent the window position to account for window decoration by Ronn */
	private int					windowOffsetX;
	private int					windowOffsetY;

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

	public JmeFXInputListener getInputListener() {
		return this.inputListener;
	}

	private JmeFxContainer(final AssetManager assetManager, final Application app, final boolean fullScreenSupport, final ICursorDisplayProvider cursorDisplayProvider) {
		this.initFx();

		final Point decorationSize = JFXUtils.getWindowDecorationSize();

		this.windowOffsetX = (int) decorationSize.getX();
		this.windowOffsetY = (int) decorationSize.getY();
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

				final EmbeddedStageInterface currentStage = JmeFxContainer.this.getStagePeer();
				try {

					if (currentStage == null) {
						return;
					}

					final int currentWidth = Display.getWidth();
					final int currentHeight = Display.getHeight();

					if (currentWidth != JmeFxContainer.this.getpWidth() || currentHeight != JmeFxContainer.this.getpHeight()) {
						JmeFxContainer.this.handleResize();
					}

					final int x = Display.getX() + (Display.isFullscreen() ? 0 : JmeFxContainer.this.getWindowOffsetX());
					final int y = Display.getY() + (Display.isFullscreen() ? 0 : JmeFxContainer.this.getWindowOffsetY());

					if (JmeFxContainer.this.getOldX() != x || JmeFxContainer.this.getOldY() != y) {

						JmeFxContainer.this.setOldX(x);
						JmeFxContainer.this.setOldY(y);

						Platform.runLater(() -> currentStage.setLocation(x, y));
					}

				} finally {
					super.updateLogicalState(tpf);
				}
			}
		};

		this.picture.move(0, 0, -1);
		this.picture.setPosition(0, 0);

		this.handleResize();

		this.tex = new Texture2D(this.jmeImage);
		this.picture.setTexture(assetManager, this.tex, true);
	}

	private int getOldX() {
		return this.oldX;
	}

	private int getOldY() {
		return this.oldY;
	}

	private void setOldX(final int oldX) {
		this.oldX = oldX;
	}

	private void setOldY(final int oldY) {
		this.oldY = oldY;
	}

	private int getpHeight() {
		return this.pHeight;
	}

	private int getpWidth() {
		return this.pWidth;
	}

	private EmbeddedStageInterface getStagePeer() {
		return this.stagePeer;
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
			//TODO 3.1 : use new Image(this.nativeFormat.get(), this.pWidth, this.pHeight, this.jmeData, com.jme3.texture.image.ColorSpace.sRGB);
			this.jmeImage = new Image(this.nativeFormat.get(), this.pWidth, this.pHeight, this.jmeData);
			//HACK pre-3.1 to support gamma correction with jme pre-implementation of ColorSpace
			try {
				Class<?> classColorSpace = Class.forName("com.jme3.texture.image.ColorSpace");
				Method m = Image.class.getMethod("setColorSpace", classColorSpace);
				m.invoke(this.jmeImage, classColorSpace.getField("sRGB").get(null));
			} catch(Throwable exc) {
				// ignore jme 3.1 not available
			}
			//HACK pre-3.1 End
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
				//TODO 3.1: use Format.ARGB8 and Format.BGRA8 and remove used of exchangeData, fx2jme_ARGB82ABGR8,...
				try {
					Format Format_ARGB8 = Format.ABGR8;
					Format Format_BGRA8 = Format.ABGR8;
					switch (Pixels.getNativeFormat()) {
					case Pixels.Format.BYTE_ARGB:
						JmeFxContainer.this.nativeFormat.complete(Format_ARGB8);
						exchangeData = JmeFxContainer::fx2jme_ARGB82ABGR8;
						break;
					case Pixels.Format.BYTE_BGRA_PRE:
						JmeFxContainer.this.nativeFormat.complete(Format_BGRA8);
						exchangeData = JmeFxContainer::fx2jme_BGRA82ABGR8;
						break;
					default:
						// this is wrong, but at least will display something
						JmeFxContainer.this.nativeFormat.complete(Format_ARGB8);
						exchangeData = JmeFxContainer::fx2jme_ARGB82ABGR8;
						break;
					}
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new RuntimeException(e);
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

	public void setScene(final Scene newScene, final Group highLevelGroup) {
		this.rootNode = highLevelGroup;
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

	private final Semaphore	imageExchange	= new Semaphore(1);
	public CursorType		lastcursor;

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

			if (!this.scenePeer.getPixels(buf, this.pWidth, this.pHeight)) {
				return;
			}

			if (this.fullScreenSuppport) {
				for (final PopupSnapper ps : this.activeSnappers) {
					ps.paint(buf, this.pWidth, this.pHeight);
				}
			}

			data.flip();
			data.limit(this.pWidth * this.pHeight * 4);
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
				// if would need to block, it means that another callable with
				// newer data will be
				// enqueued soon, so we can just ignore this repaint
				if (updateImage) {
					try {
						if (JmeFxContainer.this.fxDataReady) {
							JmeFxContainer.this.fxDataReady = false;
							JmeFxContainer.this.exchangeData.apply(JmeFxContainer.this);
							//TODO 3.1: after remove of exchangeData, uncomment swap jmeData and fxData
//							final ByteBuffer tmp = c.jmeData;
//							c.jmeData = c.fxData;
//							c.fxData = tmp;
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

	private static Void fx2jme_ARGB82ABGR8(JmeFxContainer c){
		final ByteBuffer tmp = c.jmeData;
		c.jmeData = c.fxData;
		c.fxData = tmp;
		int limit = Math.min(c.jmeData.limit(), c.fxData.limit()) - 3;
		byte v;
		for (int i = 0; i < limit; i += 4) {
			v = c.jmeData.get(i+1);
			c.jmeData.put(i + 1, c.jmeData.get(i+3) );
			c.jmeData.put(i + 3, v );
		}
		return null;
	}

	private static Void fx2jme_BGRA82ABGR8(JmeFxContainer c) {
		c.jmeData.clear();
		int limit = Math.min(c.jmeData.limit(), c.fxData.limit()) - 3;
		for (int i = 0; i <  limit; i += 4) {
			c.jmeData.put(c.fxData.get(i+3) );
			c.jmeData.put(c.fxData.get(i+0) );
			c.jmeData.put(c.fxData.get(i+1) );
			c.jmeData.put(c.fxData.get(i+2) );
		}
		c.jmeData.flip();
		return null;
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

	private final BitSet	keyStateSet	= new BitSet(0xFF);

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
											logger.warn("Cannot find snapper for window " + window);
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
				public void setTransientFocusContainer(final Scene scene, final javafx.scene.Node node) {

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

	public Group getRootNode() {
		return this.rootNode;
	}

	/**
	 * Indent the window position to account for window decoration.
	 */
	public void setWindowOffsetX(final int windowOffsetX) {
		this.windowOffsetX = windowOffsetX;
	}

	/**
	 * Indent the window position to account for window decoration.
	 */
	public void setWindowOffsetY(final int windowOffsetY) {
		this.windowOffsetY = windowOffsetY;
	}

	/**
	 * Indent the window position to account for window decoration.
	 */
	public int getWindowOffsetX() {
		return this.windowOffsetX;
	}

	/**
	 * Indent the window position to account for window decoration.
	 */
	public int getWindowOffsetY() {
		return this.windowOffsetY;
	}
}