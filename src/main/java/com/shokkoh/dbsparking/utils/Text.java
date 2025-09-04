package com.shokkoh.dbsparking.utils;

import org.bukkit.ChatColor;

public final class Text {
	private Text() {}

	public static String color(String s) {
		if (s == null) return "";
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	/**
	 * Recorta a longitud "real" (ignorando códigos de color) manteniéndolos para que no se rompa el render del scoreboard.
	 */
	public static String trimForScoreboard(String colored, int maxRealLen) {
		if (colored == null) return "";
		StringBuilder out = new StringBuilder();
		int real = 0;
		boolean code = false;
		for (char c : colored.toCharArray()) {
			if (c == ChatColor.COLOR_CHAR) {
				code = true;
				out.append(c);
				continue;
			}
			if (code) {
				code = false;
				out.append(c);
				continue;
			}
			if (real >= maxRealLen) break;
			out.append(c);
			real++;
		}
		return out.toString();
	}
}
