/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3x.jfx;

import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.embed.AbstractEvents;
import com.sun.javafx.embed.EmbeddedSceneDSInterface;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.EmbeddedStageInterface;
import com.sun.javafx.embed.HostDragStartListener;
import com.sun.javafx.embed.HostInterface;
import javafx.scene.input.TransferMode;


public class JmeFXHostContainerImpl implements HostInterface {
    JmeFxContainer fxContainer;
    
    private final JmeFxContainer jmeFxContainer;
    
    public JmeFXHostContainerImpl (JmeFxContainer jmeFxContainer) {
        this.jmeFxContainer = jmeFxContainer;       
    }
    
        @Override
        public void setEmbeddedStage(final EmbeddedStageInterface embeddedStage) {
            jmeFxContainer.stagePeer = embeddedStage;
            if (jmeFxContainer.stagePeer == null) {
                return;
            }
            if (jmeFxContainer.pWidth > 0 && jmeFxContainer.pHeight > 0) {
                jmeFxContainer.stagePeer.setSize(jmeFxContainer.pWidth, jmeFxContainer.pHeight);
            }

            jmeFxContainer.stagePeer.setFocused(true, AbstractEvents.FOCUSEVENT_ACTIVATED);
        }

        @Override
        public void setEmbeddedScene(final EmbeddedSceneInterface embeddedScene) {
            jmeFxContainer.scenePeer = embeddedScene;
            if (jmeFxContainer.scenePeer == null) {
                return;
            }
            if (jmeFxContainer.pWidth > 0 && jmeFxContainer.pHeight > 0) {
                jmeFxContainer.scenePeer.setSize(jmeFxContainer.pWidth, jmeFxContainer.pHeight);
            }

            jmeFxContainer.scenePeer.setDragStartListener(new HostDragStartListener() {
                @Override
                public void dragStarted(final EmbeddedSceneDSInterface dragSource, final TransferMode dragAction) {
                    System.out.println("Dragging");
                }
            });

        }

        @Override
        public boolean requestFocus() {
            // System.out.println("Called requestFocus");
            return true;
        }

        @Override
        public boolean traverseFocusOut(final boolean forward) {
            System.out.println("Called traverseFocusOut " + forward);
            return true;
        }

        @Override
        public void setPreferredSize(final int width, final int height) {
        }
        int repaintCounter = 0;

        @Override
        public void repaint() {
            // System.out.println("Repainting from host " + (++repaintCounter) + " " + System.currentTimeMillis()%10000);
            jmeFxContainer.paintComponent();
        }

        @Override
        public void setEnabled(final boolean enabled) {
            jmeFxContainer.setFxEnabled(enabled);
        }

        @Override
        public void setCursor(final CursorFrame cursorFrame) {
           jmeFxContainer.cursorDisplayProvider.showCursor(cursorFrame);
        }

        @Override
        public boolean grabFocus() {
            // System.out.println("Called grabFocus");
            return true;
        }

        @Override
        public void ungrabFocus() {
            // System.out.println("Called ungrabFocus");
        }

}
