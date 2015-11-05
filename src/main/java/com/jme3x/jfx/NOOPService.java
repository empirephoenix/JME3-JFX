package com.jme3x.jfx;

import javafx.stage.Stage;

import com.jme3x.jfx.window.AbstractWindow;

/**
 * Dummy default service for api compability
 * 
 * @author empire
 *
 */
public class NOOPService implements IRememberMeService {
	@Override
	public void onAttach(final AbstractHud hud) {
	}

	@Override
	public void onRemove(final AbstractHud hud) {
	}

	@Override
	public void onExternal(final AbstractWindow hud, final Stage externalStage) {
	}
}
