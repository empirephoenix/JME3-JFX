package com.jme3x.jfx;

import java.util.concurrent.Callable;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.ui.Picture;
import com.jme3.util.BufferUtils;
import com.jme3x.jfx.cursor.ICursorDisplayProvider;
import com.sun.javafx.embed.EmbeddedStageInterface;

import javafx.application.Platform;
import javafx.scene.Scene;

public class JmeFxScreenContainer extends JmeFxContainer {

    private final Picture picture;
    private final DisplayInfo displayInfo;

    public JmeFxScreenContainer(AssetManager assetManager, Application app, boolean fullScreenSupport, ICursorDisplayProvider cursorDisplayProvider) {
        super();

        this.cursorDisplayProvider = cursorDisplayProvider;
        this.app = app;
        this.fullScreenSuppport = fullScreenSupport;
        this.displayInfo = DisplayInfoProvider.find(app);

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

                final EmbeddedStageInterface currentStage = getStagePeer();
                try {

                    if(currentStage == null) {
                        return;
                    }

                    if (stage != null && displayInfo.isFullscreen() ) {
                        sceneContainerMap.put(stage, JmeFxScreenContainer.this);
                    } else {
                        sceneContainerMap.remove(stage);
                    }

                    final int currentWidth = displayInfo.getWidth();
                    final int currentHeight = displayInfo.getHeight();

                    if(currentWidth != pWidth || currentHeight != pHeight) {
                        handleResize();
                    }

                    final int x = displayInfo.getX() + (displayInfo.isFullscreen() ? 0 : displayInfo.getInsetX());
                    final int y = displayInfo.getY() + (displayInfo.isFullscreen() ? 0 : displayInfo.getInsetY());

                    if(oldX != x || oldY != y) {

                        oldX = x;
                        oldY = y;

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


    public Picture getJmeNode() {
        return this.picture;
    }

    @Override
    public int getWindowX() {
        return this.oldX;
    }

    @Override
    public int getWindowY() {
        return this.oldY;
    }

    private void handleResize() {

        try {
            this.imageExchange.acquire();
            dispose();

            this.pWidth = displayInfo.getWidth();
            this.pHeight = displayInfo.getHeight();
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
            this.jmeImage = new Image(this.nativeFormat.get(), this.pWidth, this.pHeight, this.jmeData, ColorSpace.sRGB);
            if (this.tex != null) {
                this.tex.setImage(this.jmeImage);
            }

            if (this.stagePeer != null) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        JmeFxScreenContainer.this.stagePeer.setSize(JmeFxScreenContainer.this.pWidth, JmeFxScreenContainer.this.pHeight);
                        JmeFxScreenContainer.this.scenePeer.setSize(JmeFxScreenContainer.this.pWidth, JmeFxScreenContainer.this.pHeight);
                        JmeFxScreenContainer.this.hostContainer.repaint();
                    }
                });
            }

        } catch (final Exception exc) {
            exc.printStackTrace();
        } finally {
            this.imageExchange.release();
        }
    }


    @Override
    protected void setSceneImpl(Scene newScene) {
        super.setSceneImpl(newScene);
        this.app.enqueue(new Callable<Void>() {

            @Override
            public Void call() {
                JmeFxScreenContainer.this.picture.setCullHint(newScene == null ? CullHint.Always : CullHint.Never);
                return null;
            }
        });
    }

    @Override
    public int getXPosition() {
        if (!displayInfo.isFullscreen()) {
            return displayInfo.getX() + displayInfo.getInsetX();
        }
        return 0;
    }

    @Override
    public int getYPosition() {
        if (!displayInfo.isFullscreen()) {
            return displayInfo.getY() + displayInfo.getInsetY();
        }
        return 0;
    }


}
