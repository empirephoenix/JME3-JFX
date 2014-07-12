package com.jme3x.jfx;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3x.jfx.media.TextureMovie;
import com.jme3x.jfx.media.TextureMovie.LetterboxMode;
import com.sun.javafx.application.PlatformImpl;

public class TestMovie extends SimpleApplication {

    private TextureMovie textureMovie;
    private MediaPlayer mp;

    public static void main(String[] args) {

        PlatformImpl.startup(() -> {});

        TestMovie app = new TestMovie();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        Media media = new Media("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");
        mp = new MediaPlayer(media);
        mp.play();

        textureMovie = new TextureMovie(mp, LetterboxMode.VALID_LETTERBOX);
        textureMovie.setLetterboxColor(ColorRGBA.Black);

        Geometry screen1 = new Geometry("Screen1", new Quad(20, 20));

        Material s1mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        s1mat.setTexture("ColorMap", textureMovie.getTexture());
        screen1.setMaterial(s1mat);
        rootNode.attachChild(screen1);

        cam.setLocation(new Vector3f(10, 10, 15));

    }

    @Override
    public void destroy() {
        super.destroy();
        mp.stop();
        PlatformImpl.exit();
    }

}
