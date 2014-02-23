package com.jme3x.jfx;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.lwjgl.opengl.Display;

public class Test extends SimpleApplication {

    public static void main(final String[] args) {
        new Test().start();
    }

    @Override
    public void simpleInitApp() {
        this.setPauseOnLostFocus(false);
        this.flyCam.setDragToRotate(true);
        this.viewPort.setBackgroundColor(ColorRGBA.Red);

        final GuiManager testguiManager = new GuiManager(this.guiNode, this.assetManager, this, false,
                new ExampleCursorProvider(this, this.assetManager, this.inputManager));

        FxPlatformExecutor.runOnFxApplication(new Runnable() {
            @Override
            public void run() {
                Group root = new Group();
                Scene scene = new Scene(root, 500, 500, Color.BLACK);

                final FXMLLoader fxmlLoader = new FXMLLoader();
                final URL location = this.getClass().getResource("test.fxml");
                fxmlLoader.setLocation(location);
                fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
                try {
                    root.getChildren().add((Node)
                            fxmlLoader.load(fxmlLoader.getLocation().openStream()));
                   ((TestFxController) fxmlLoader.getController()).initialize();
                } catch (IOException ex) {
                    Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
                }
               

                Rectangle r = new Rectangle(25, 25, 250, 250);
                r.setFill(Color.BLUE);
                //root.getChildren().add(r);
                testguiManager.getjmeFXContainer().setScene(scene);



            }
        });



        /**
         * 2d gui, use the default input provider
         *
         * this.inputManager.addRawInputListener(testguiManager.getInputRedirector());
         * final TestHud testhud = new TestHud(); testhud.initialize();
         * testguiManager.attachHudAsync(testhud);
         *
         *
         * final TestWindow testwindow = new TestWindow();
         * testwindow.initialize(); testwindow.setTitleAsync("TestTitle");
         * testguiManager.attachHudAsync(testwindow);
         * Display.setResizable(true);
         */
    }

    @Override
    public void simpleUpdate(final float tpf) {
        if (Display.wasResized()) {
            // keep settings in sync with the actual Display
            int w = Display.getWidth();
            int h = Display.getHeight();
            if (w < 2) {
                w = 2;
            }
            if (h < 2) {
                h = 2;
            }
            this.settings.setWidth(Display.getWidth());
            this.settings.setHeight(Display.getHeight());
            this.reshape(this.settings.getWidth(), this.settings.getHeight());
        }
    }
}
