/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3x.jfx;

import javafx.application.Platform;

/**
 *
 * @author Heist
 */
public class FxPlatformExecutor {
    
    public static void runOnFxApplication(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }
}
