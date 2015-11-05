package com.jme3x.jfx.injfx;

import java.util.HashMap;
import java.util.Map;

import com.jme3.app.Application;

import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class CameraDriverInput {
	static class KeyActions {
		public Runnable onPressed;
		public Runnable onReleased;

		public KeyActions(Runnable onPressed, Runnable onReleased) {
			super();
			this.onPressed = onPressed;
			this.onReleased = onReleased;
		}
	}
	
	public static void bindDefaults(ImageView c, CameraDriverInput driver) {
		c.setFocusTraversable(true);
		c.hoverProperty().addListener((ob, o, n)->{
			if(n) c.requestFocus();
		});
		c.addEventFilter(MouseEvent.MOUSE_CLICKED, (e)->{c.requestFocus();});
		
		Map<KeyCode, KeyActions> inputMap = new HashMap<>();
		inputMap.put(KeyCode.PAGE_UP, new KeyActions(driver::upPressed, driver::upReleased));
		inputMap.put(KeyCode.PAGE_DOWN, new KeyActions(driver::downPressed, driver::downReleased));

		// arrow
		inputMap.put(KeyCode.UP, new KeyActions(driver::forwardPressed, driver::forwardReleased));
		inputMap.put(KeyCode.LEFT, new KeyActions(driver::leftPressed, driver::leftReleased));
		inputMap.put(KeyCode.DOWN, new KeyActions(driver::backwardPressed, driver::backwardReleased));
		inputMap.put(KeyCode.RIGHT, new KeyActions(driver::rightPressed, driver::rightReleased));

		//WASD
		inputMap.put(KeyCode.W, new KeyActions(driver::forwardPressed, driver::forwardReleased));
		inputMap.put(KeyCode.A, new KeyActions(driver::leftPressed, driver::leftReleased));
		inputMap.put(KeyCode.S, new KeyActions(driver::backwardPressed, driver::backwardReleased));
		inputMap.put(KeyCode.D, new KeyActions(driver::rightPressed, driver::rightReleased));

		// ZQSD
		inputMap.put(KeyCode.Z, new KeyActions(driver::forwardPressed, driver::forwardReleased));
		inputMap.put(KeyCode.Q, new KeyActions(driver::leftPressed, driver::leftReleased));
		c.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
			KeyActions ka = inputMap.get(event.getCode());
			if (ka != null) ka.onPressed.run();
	        event.consume();
	    });
		c.addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
			KeyActions ka = inputMap.get(event.getCode());
			if (ka != null) ka.onReleased.run();
	        event.consume();
	    });
	}

	// mapping should be improve (allow remapping, multi key mapping, avoid same key for several action, what is done by java Key Bindings ;-))

	public Application jme;
	public float speed = 1.0f;


	protected CameraDriverAppState driver() {
		if (jme == null) return null;
		return jme.getStateManager().getState(CameraDriverAppState.class);
	}

	public void upPressed(){
		CameraDriverAppState d = driver();
		if (d != null) d.up = 1.0f * speed;
	}
	public void upReleased(){
		CameraDriverAppState d = driver();
		if (d != null) d.up = 0.0f * speed;
	}
	public void downPressed(){
		CameraDriverAppState d = driver();
		if (d != null) d.down = 1.0f * speed;
	}
	public void downReleased(){
		CameraDriverAppState d = driver();
		if (d != null) d.down = 0.0f * speed;
	}
	public void rightPressed(){
		CameraDriverAppState d = driver();
		if (d != null) d.right = 1.0f * speed;
	}
	public void rightReleased(){
		CameraDriverAppState d = driver();
		if (d != null) d.right = 0.0f * speed;
	}
	public void leftPressed(){
		CameraDriverAppState d = driver();
		if (d != null) d.left = 1.0f * speed;
	}
	public void leftReleased(){
		CameraDriverAppState d = driver();
		if (d != null) d.left = 0.0f * speed;
	}
	public void forwardPressed(){
		CameraDriverAppState d = driver();
		if (d != null) d.forward = 1.0f * speed;
	}
	public void forwardReleased(){
		CameraDriverAppState d = driver();
		if (d != null) d.forward = 0.0f * speed;
	}
	public void backwardPressed(){
		CameraDriverAppState d = driver();
		if (d != null) d.backward = 1.0f * speed;
	}
	public void backwardReleased(){
		CameraDriverAppState d = driver();
		if (d != null) d.backward = 0.0f * speed;
	}

}
