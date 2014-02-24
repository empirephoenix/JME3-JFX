/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3x.jfx;

import com.sun.javafx.perf.PerformanceTracker;
import java.lang.reflect.Field;
import java.nio.IntBuffer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Window;

public class PopupSnapper {

    private Window window;
    private Scene scene;
    WritableImage img;
    boolean ignoreRepaint;
    JmeFxContainer jmeFXcontainerReference;

    public PopupSnapper(JmeFxContainer containReference, final Window window, final Scene scene) {
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

                final int xoff = (int) this.window.getX() - jmeFXcontainerReference.getWindowX();
                final int yoff = (int) this.window.getY() - jmeFXcontainerReference.getWindowY();

                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        final int offset = x + xoff + (y + yoff) * pWidth;
                        final int old = buf.get(offset);
                        final int boff = 4 * (x + y * w);
                        final int toMerge = pixels[boff] | pixels[boff + 1] << 8 | pixels[boff + 2] << 16
                                | pixels[boff + 3] << 24;

                        final int merge = PixelUtils.mergeBgra(old, toMerge);
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
            jmeFXcontainerReference.paintComponent();
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

        jmeFXcontainerReference.activeSnappers.add(this);

    }

    public void stop() {
        jmeFXcontainerReference.activeSnappers.remove(this);
    }
}
