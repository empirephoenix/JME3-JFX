package com.jme3x.jfx.window;

import javafx.util.Callback;

public class FXMLControllerFactoryHook {

	private static Callback<Class<?>, Object>	factory;
	private static Callback<Class<?>, Void>		loadNotifier;

	public static Callback<Class<?>, Object> getFactory() {
		return FXMLControllerFactoryHook.factory;
	}

	public static void setFactory(final Callback<Class<?>, Object> factory) {
		FXMLControllerFactoryHook.factory = factory;
	}

	public static Callback<Class<?>, Void> getLoadNotifier() {
		return FXMLControllerFactoryHook.loadNotifier;
	}

	public static void setLoadNotifier(final Callback<Class<?>, Void> loadNotifier) {
		FXMLControllerFactoryHook.loadNotifier = loadNotifier;
	}

}
