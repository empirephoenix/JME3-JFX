/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3x.jfx;

import java.awt.event.KeyEvent;
import java.util.BitSet;

import org.lwjgl.opengl.Display;

import com.jme3.input.RawInputListener;
import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.sun.javafx.embed.AbstractEvents;

/**
 * Converts JMEEvents to JFXEvents
 * 
 * @author Heist
 */
public class JmeFXInputListener implements RawInputListener {

	JmeFxContainer	jmeFxContainer;
	private BitSet	keyStateSet	= new BitSet(0xFF);
	private char[]	keyCharSet	= new char[0xFF];

	public JmeFXInputListener(final JmeFxContainer listensOnContainer) {
		this.jmeFxContainer = listensOnContainer;
	}

	;

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

		if (this.jmeFxContainer.scenePeer == null) {
			return;
		}

		final int x = evt.getX();
		final int y = (int) Math.round(this.jmeFxContainer.getScene().getHeight()) - evt.getY();

		final boolean covered = this.jmeFxContainer.isCovered(x, y);
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

		this.jmeFxContainer.scenePeer.mouseEvent(type, button, this.mouseButtonState[0], this.mouseButtonState[1],
				this.mouseButtonState[2], x, y, Display.getX() + x, Display.getY() + y,
				this.keyStateSet.get(KeyEvent.VK_SHIFT), this.keyStateSet.get(KeyEvent.VK_CONTROL),
				this.keyStateSet.get(KeyEvent.VK_ALT), this.keyStateSet.get(KeyEvent.VK_META), wheelRotation, false);
	}

	boolean[]	mouseButtonState	= new boolean[3];

	@Override
	public void onMouseButtonEvent(final MouseButtonEvent evt) {

		// TODO: Process events in separate thread ?
		if (this.jmeFxContainer.scenePeer == null) {
			return;
		}

		final int x = evt.getX();
		final int y = (int) Math.round(this.jmeFxContainer.getScene().getHeight()) - evt.getY();

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

		final boolean covered = this.jmeFxContainer.isCovered(x, y);
		if (!covered) {
			this.jmeFxContainer.loseFocus();
		} else {
			evt.setConsumed();
			this.jmeFxContainer.grabFocus();
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

		this.jmeFxContainer.scenePeer.mouseEvent(type, button, this.mouseButtonState[0], this.mouseButtonState[1],
				this.mouseButtonState[2], x, y, Display.getX() + x, Display.getY() + y,
				this.keyStateSet.get(KeyEvent.VK_SHIFT), this.keyStateSet.get(KeyEvent.VK_CONTROL),
				this.keyStateSet.get(KeyEvent.VK_ALT), this.keyStateSet.get(KeyEvent.VK_META), 0,
				button == AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON);

	}

	@Override
	public void onKeyEvent(final KeyInputEvent evt) {

		if (this.jmeFxContainer.scenePeer == null) {
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

		if (this.jmeFxContainer.focus) {
			evt.setConsumed();
		}

		if (evt.isRepeating()) {
			final char x = this.keyCharSet[fxKeycode];

			if (this.jmeFxContainer.focus) {
				this.jmeFxContainer.scenePeer.keyEvent(AbstractEvents.KEYEVENT_TYPED, fxKeycode, new char[] { x },
						keyState);
			}
		} else if (evt.isPressed()) {
			this.keyCharSet[fxKeycode] = keyChar;
			this.keyStateSet.set(fxKeycode);
			if (this.jmeFxContainer.focus) {
				this.jmeFxContainer.scenePeer.keyEvent(AbstractEvents.KEYEVENT_PRESSED, fxKeycode,
						new char[] { keyChar }, keyState);
				this.jmeFxContainer.scenePeer.keyEvent(AbstractEvents.KEYEVENT_TYPED, fxKeycode,
						new char[] { keyChar }, keyState);
			}
		} else {
			final char x = this.keyCharSet[fxKeycode];
			this.keyStateSet.clear(fxKeycode);
			if (this.jmeFxContainer.focus) {
				this.jmeFxContainer.scenePeer.keyEvent(AbstractEvents.KEYEVENT_RELEASED, fxKeycode, new char[] { x },
						keyState);
			}
		}

	}

	@Override
	public void onTouchEvent(final TouchEvent evt) {
	}

	public int retrieveKeyState() {
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
}
