package com.jme3x.jfx.windowpos;

import javafx.stage.Stage;

import com.jme3x.jfx.AbstractHud;
import com.jme3x.jfx.window.AbstractWindow;

/**
 * Create your
 */
public interface IRememberMeService {
	public void onAttach(AbstractHud hud);

	public void onRemove(AbstractHud hud);

	public void onExternal(AbstractWindow window, Stage externalStage);
}
