package com.jme3x.jfx;

import com.jme3.app.Application;
import com.jme3.system.JmeContext;
import com.jme3.system.jogl.DisplayInfo_Jogl;

public class DisplayInfoProvider {

	public static DisplayInfo find(Application app) {
		JmeContext ctx  = app.getContext();
		if (ctx.getClass().getName().startsWith("com.jme3.system.jogl.JoglNewt")) {
			return new DisplayInfo_Jogl((com.jme3.system.jogl.JoglNewtAbstractDisplay) ctx);
		}
		if (ctx.getClass().getName().startsWith("com.jme3.system.lwjgl")) {
			return new DisplayInfo_Lwjgl();
		}
		return new DisplayInfo_AppSettings(ctx.getSettings());
	}
}
