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
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import org.lwjgl.opengl.Display;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.asset.AssetManager;
import com.jme3.input.RawInputListener;
import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
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
import com.sun.javafx.perf.PerformanceTracker;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.scene.SceneHelper.SceneAccessor;
import com.sun.javafx.stage.EmbeddedWindow;

/**
 * Need to pass -Dprism.dirtyopts=false on startup
 *
 * @author abies / Artur Biesiadowski
 */
public class JmeFxContainer implements RawInputListener {

    EmbeddedStageInterface stagePeer;
    EmbeddedSceneInterface scenePeer;
    volatile EmbeddedWindow stage;
    HostInterface hostContainer;
    int pWidth;
    int pHeight;
    volatile Scene scene;
    Picture picture;
    Image jmeImage;
    Texture2D tex;
    ByteBuffer jmeData;
    ByteBuffer fxData;
    boolean fxDataReady = false;
    int oldX = -1;
    int oldY = -1;
    boolean focus;
    Application app;
    boolean fullScreenSuppport;
    CompletableFuture<Format> nativeFormat = new CompletableFuture<Format>();
    ICursorDisplayProvider cursorDisplayProvider;

    public static JmeFxContainer install(final Application app, final Node guiNode, final boolean fullScreenSupport,
            final ICursorDisplayProvider cursorDisplayProvider) {
        final JmeFxContainer ctr = new JmeFxContainer(app.getAssetManager(), app, fullScreenSupport,
                cursorDisplayProvider);
        guiNode.attachChild(ctr.getJmeNode());
        app.getInputManager().addRawInputListener(ctr);

        if (fullScreenSupport) {
            ctr.installSceneAccessorHack();
        }

        return ctr;
    }

    private JmeFxContainer(final AssetManager assetManager, final Application app, final boolean fullScreenSupport,
            final ICursorDisplayProvider cursorDisplayProvider) {

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

        this.hostContainer = new JmeFXHostContainerImpl(this);
        this.picture = new Picture("JavaFXContainer", true) {
            @Override
            public void updateLogicalState(final float tpf) {
                if (JmeFxContainer.this.stagePeer != null) {

                    if (Display.getWidth() != JmeFxContainer.this.pWidth
                            || Display.getHeight() != JmeFxContainer.this.pHeight) {
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
    private Semaphore imageExchange = new Semaphore(1);
    public CursorType lastcursor;

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

    @Override
    public void beginInput() {
    }

    @Override
    public void endInput() {
    }

    @Override
    public void onJoyAxisEvent(final JoyAxisEvent evt) {
    }

    @Override
    public void onJoyButtonEvent(final JoyButtonEvent evt) {
    }

    @Override
    public void onMouseMotionEvent(final MouseMotionEvent evt) {

        if (this.scenePeer == null) {
            return;
        }

        final int x = evt.getX();
        final int y = this.pHeight - evt.getY();

        final boolean covered = this.isCovered(x, y);
        if (covered) {
            evt.setConsumed();
        }

        // not sure if should be grabbing focus on mouse motion event
        // grabFocus();

        int type = AbstractEvents.MOUSEEVENT_MOVED;
        int button = AbstractEvents.MOUSEEVENT_NONE_BUTTON;

        final int wheelRotation = (int) Math.round(evt.getDeltaWheel() / -120.0);

        if (wheelRotation != 0) {
            type = AbstractEvents.MOUSEEVENT_WHEEL;
            button = AbstractEvents.MOUSEEVENT_NONE_BUTTON;
        } else if (this.mouseButtonState[0]) {
            type = AbstractEvents.MOUSEEVENT_DRAGGED;
            button = AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON;
        } else if (this.mouseButtonState[1]) {
            type = AbstractEvents.MOUSEEVENT_DRAGGED;
            button = AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON;
        } else if (this.mouseButtonState[2]) {
            type = AbstractEvents.MOUSEEVENT_DRAGGED;
            button = AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON;
        }

        this.scenePeer.mouseEvent(type, button, this.mouseButtonState[0], this.mouseButtonState[1],
                this.mouseButtonState[2], x, y, Display.getX() + x, Display.getY() + y,
                this.keyStateSet.get(KeyEvent.VK_SHIFT), this.keyStateSet.get(KeyEvent.VK_CONTROL),
                this.keyStateSet.get(KeyEvent.VK_ALT), this.keyStateSet.get(KeyEvent.VK_META), wheelRotation, false);
    }
    boolean[] mouseButtonState = new boolean[3];

    @Override
    public void onMouseButtonEvent(final MouseButtonEvent evt) {

        // TODO: Process events in separate thread ?
        if (this.scenePeer == null) {
            return;
        }

        final int x = evt.getX();
        final int y = this.pHeight - evt.getY();

        int button;

        switch (evt.getButtonIndex()) {
            case 0:
                button = AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON;
                break;
            case 1:
                button = AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON;
                break;
            case 2:
                button = AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON;
                break;
            default:
                return;
        }

        this.mouseButtonState[evt.getButtonIndex()] = evt.isPressed();

        // seems that generating mouse release without corresponding mouse pressed is causing problems in Scene.ClickGenerator

        final boolean covered = this.isCovered(x, y);
        if (!covered) {
            this.loseFocus();
        } else {
            evt.setConsumed();
            this.grabFocus();
        }

        int type;
        if (evt.isPressed()) {
            type = AbstractEvents.MOUSEEVENT_PRESSED;
        } else if (evt.isReleased()) {
            type = AbstractEvents.MOUSEEVENT_RELEASED;
            // and clicked ??
        } else {
            return;
        }

        this.scenePeer.mouseEvent(type, button, this.mouseButtonState[0], this.mouseButtonState[1],
                this.mouseButtonState[2], x, y, Display.getX() + x, Display.getY() + y,
                this.keyStateSet.get(KeyEvent.VK_SHIFT), this.keyStateSet.get(KeyEvent.VK_CONTROL),
                this.keyStateSet.get(KeyEvent.VK_ALT), this.keyStateSet.get(KeyEvent.VK_META), 0,
                button == AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON);

    }

    private boolean isCovered(final int x, final int y) {
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
    private char[] keyCharSet = new char[0xFF];
    private BitSet keyStateSet = new BitSet(0xFF);

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

    @Override
    public void onKeyEvent(final KeyInputEvent evt) {

        if (this.scenePeer == null) {
            return;
        }

        final char keyChar = evt.getKeyChar();

        int fxKeycode = AwtKeyInput.convertJmeCode(evt.getKeyCode());

        final int keyState = this.retrieveKeyState();
        if (fxKeycode > this.keyCharSet.length) {
            switch (keyChar) {
                case '\\':
                    fxKeycode = java.awt.event.KeyEvent.VK_BACK_SLASH;
                    break;
                default:
                    return;
            }
        }

        if (this.focus) {
            evt.setConsumed();
        }

        if (evt.isRepeating()) {
            final char x = this.keyCharSet[fxKeycode];

            if (this.focus) {
                this.scenePeer.keyEvent(AbstractEvents.KEYEVENT_TYPED, fxKeycode, new char[]{x}, keyState);
            }
        } else if (evt.isPressed()) {
            this.keyCharSet[fxKeycode] = keyChar;
            this.keyStateSet.set(fxKeycode);
            if (this.focus) {
                this.scenePeer.keyEvent(AbstractEvents.KEYEVENT_PRESSED, fxKeycode, new char[]{keyChar}, keyState);
                this.scenePeer.keyEvent(AbstractEvents.KEYEVENT_TYPED, fxKeycode, new char[]{keyChar}, keyState);
            }
        } else {
            final char x = this.keyCharSet[fxKeycode];
            this.keyStateSet.clear(fxKeycode);
            if (this.focus) {
                this.scenePeer.keyEvent(AbstractEvents.KEYEVENT_RELEASED, fxKeycode, new char[]{x}, keyState);
            }
        }

    }

    @Override
    public void onTouchEvent(final TouchEvent evt) {
    }
    Map<Window, PopupSnapper> snappers = new IdentityHashMap<>();
    List<PopupSnapper> activeSnappers = new CopyOnWriteArrayList<>();

    class PopupSnapper {

        private Window window;
        private Scene scene;
        WritableImage img;
        boolean ignoreRepaint;

        public PopupSnapper(final Window window, final Scene scene) {
            this.window = window;
            this.scene = scene;
        }

        public void paint(final IntBuffer buf, final int pWidth, final int pHeight) {

            // fixme to preserve proper colors

            try {

                final WritableImage img = this.img;
                if (img == null) {
                    return;
                }
                synchronized (this) {
                    final PixelReader pr = img.getPixelReader();

                    final int w = (int) img.getWidth();
                    final int h = (int) img.getHeight();

                    final byte[] pixels = new byte[w * h * 4];
                    pr.getPixels(0, 0, w, h, PixelFormat.getByteBgraPreInstance(), pixels, 0, w * 4);

                    final int xoff = (int) this.window.getX() - JmeFxContainer.this.oldX;
                    final int yoff = (int) this.window.getY() - JmeFxContainer.this.oldY;

                    for (int x = 0; x < w; x++) {
                        for (int y = 0; y < h; y++) {
                            final int offset = x + xoff + (y + yoff) * pWidth;
                            final int old = buf.get(offset);
                            final int boff = 4 * (x + y * w);
                            final int toMerge = pixels[boff] | pixels[boff + 1] << 8 | pixels[boff + 2] << 16
                                    | pixels[boff + 3] << 24;

                            final int merge = JmeFxContainer.mergeBgra(old, toMerge);
                            buf.put(offset, merge);
                        }
                    }
                }
            } catch (final Exception exc) {
                exc.printStackTrace();
            }

        }

        public void repaint() {
            try {
                if (!Color.TRANSPARENT.equals(this.scene.getFill())) {
                    this.scene.setFill(Color.TRANSPARENT);
                }

                if (this.img != null) {
                    if (this.img.getWidth() != this.scene.getWidth() || this.img.getHeight() != this.scene.getHeight()) {
                        this.img = null;
                    }
                }
                synchronized (this) {
                    this.img = this.scene.snapshot(this.img);
                }
                JmeFxContainer.this.paintComponent();
            } catch (final Exception exc) {
                exc.printStackTrace();
            }
        }

        public void start() {

            try {
                final Field trackerField = Scene.class.getDeclaredField("tracker");
                trackerField.setAccessible(true);
                trackerField.set(this.scene, new PerformanceTracker() {
                    @Override
                    public void frameRendered() {
                        super.frameRendered();
                        if (PopupSnapper.this.ignoreRepaint) {
                            PopupSnapper.this.ignoreRepaint = false;
                            return;
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                PopupSnapper.this.ignoreRepaint = true;
                                PopupSnapper.this.repaint();
                            }
                        });

                    }

                    @Override
                    public void pulse() {
                        super.pulse();
                    }

                    @Override
                    protected long nanoTime() {
                        return System.nanoTime();
                    }

                    @Override
                    public void doOutputLog() {
                    }

                    @Override
                    public void doLogEvent(final String s) {
                    }
                });
            } catch (final Exception exc) {
                exc.printStackTrace();
            }

            JmeFxContainer.this.activeSnappers.add(this);

        }

        public void stop() {
            JmeFxContainer.this.activeSnappers.remove(this);
        }
    }

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
                        public void changed(final javafx.beans.value.ObservableValue<? extends Window> observable,
                                final Window oldValue, final Window window) {
                            window.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
                                @Override
                                public void handle(final WindowEvent event) {
                                    if (Display.isFullscreen()) {
                                        final PopupSnapper ps = new PopupSnapper(window, scene);
                                        JmeFxContainer.this.snappers.put(window, ps);
                                        ps.start();
                                    }
                                }
                            });
                        }
                    ;
                    });

					scene.windowProperty().addListener(new ChangeListener<Window>() {
                        @Override
                        public void changed(final javafx.beans.value.ObservableValue<? extends Window> observable,
                                final Window oldValue, final Window window) {
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
                        }
                    ;
                    });

					return scene;
                }
            };

            f.set(null, sa);
        } catch (final Exception exc) {
            exc.printStackTrace();
        }
    }

    static int mergeArgb(final int bg, final int src) {

        final int sa = src >>> 24;

        if (sa == 0) {
            return bg;
        }

        final int ba = bg >>> 24;

        final int rb = (src & 0x00ff00ff) * sa + (bg & 0x00ff00ff) * (0xff - sa) & 0xff00ff00;
        final int g = (src & 0x0000ff00) * sa + (bg & 0x0000ff00) * (0xff - sa) & 0x00ff0000;
        final int a = sa + (ba * (0xff - sa) >> 8);

        return a << 24 | (rb | g) >>> 8;
    }

    static int mergeBgra(final int bg, final int src) {

        final int sa = src & 0xff;

        if (sa == 0) {
            return bg;
        }

        final int ba = bg & 0xff;

        final int a = sa + (ba * (0xff - sa) >> 8);

        final int b = ((src & 0xff000000) >> 24) * sa + ((bg & 0xff000000) >> 24) * ba >> 8;
        final int g = ((src & 0xff0000) >> 16) * sa + ((bg & 0xff0000) >> 16) * ba >> 8;
        final int r = ((src & 0xff00) >> 8) * sa + ((bg & 0xff00) >> 8) * ba >> 8;

        return b << 24 | g << 16 | r << 8 | a;
        // return 0xffff0000;
    }

    /*
     * public static javafx.scene.image.Image load(Image jmeImage) { Format format = jmeImage.getFormat(); int width = jmeImage.getWidth(); int height = jmeImage.getHeight(); ByteBuffer data = jmeImage.getData(0);
     * 
     * WritableImage wi = new WritableImage(width,height);
     * 
     * PixelWriter pw = wi.getPixelWriter();
     * 
     * 
     * 
     * 
     * PixelFormat<ByteBuffer> pf;
     * 
     * switch (format) { case RGB8: pf = PixelFormat.getByteRgbInstance(); break; case BGRA8: pf = PixelFormat.getByteBgra(); break; default: return null; }
     * 
     * pw.setPixels(0,0,width,height,pf,data,format.getBitsPerPixel()/8);
     * 
     * return wi; }
     */
}