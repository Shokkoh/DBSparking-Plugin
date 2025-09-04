package com.shokkoh.dbsparking.utils;


public final class Logger {
	private static final String PREFIX = "&8[&eD&cB&bSparking!&8] ";

	private Logger() {}

	/**
	 * Envía un mensaje de nivel INFO (blanco) a la consola.
	 * @param message El mensaje a enviar.
	 */
	public static void info(String message) {
		System.out.println(PREFIX + "&a" + message);
	}

	/**
	 * Envía un mensaje de nivel WARNING (amarillo) a la consola.
	 * @param message El mensaje a enviar.
	 */
	public static void warning(String message) {
		System.out.println(PREFIX + "&e" +message);
	}

	/**
	 * Envía un mensaje de nivel SEVERE/ERROR (rojo) a la consola.
	 * @param message El mensaje a enviar.
	 */
	public static void severe(String message) {
		System.out.println(PREFIX + "&c" + message);
	}

	/**
	 * Envía un mensaje sin nivel, usando los colores que ya contenga.
	 * @param message El mensaje a enviar.
	 */
	public static void raw(String message) {
		System.out.println(PREFIX + message);
	}
}
