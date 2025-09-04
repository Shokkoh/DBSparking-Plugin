package com.shokkoh.dbsparking.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TitleReflection {

	private static class PlayerTitleState {
		String title;
		String subtitle;
		int fadeIn;
		int stay;
		int fadeOut;
	}

	private static final Map<UUID, PlayerTitleState> playerTitleStates = new HashMap<>();

	/**
	 * Envía un título/subtítulo de forma aditiva. Recuerda el estado anterior.
	 */
	public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		if (player == null || !player.isOnline()) {
			return;
		}

		PlayerTitleState state = playerTitleStates.getOrDefault(player.getUniqueId(), new PlayerTitleState());

		if (title != null) {
			state.title = title.isEmpty() ? null : title;
		}
		if (subtitle != null) {
			state.subtitle = subtitle.isEmpty() ? null : subtitle;
		}

		state.fadeIn = fadeIn;
		state.stay = stay;
		state.fadeOut = fadeOut;

		playerTitleStates.put(player.getUniqueId(), state);

		try {
			String finalTitle = (state.title != null) ? ChatColor.translateAlternateColorCodes('&', state.title) : null;
			String finalSubtitle = (state.subtitle != null) ? ChatColor.translateAlternateColorCodes('&', state.subtitle) : null;

			Method sendTitleMethod = player.getClass().getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
			sendTitleMethod.invoke(player, finalTitle, finalSubtitle, state.fadeIn, state.stay, state.fadeOut);

		} catch (Exception e) {
			System.err.println("[DBSparking] Failed to send title via reflection. Is NecroTempus mod installed?");
			e.printStackTrace();
		}
	}

	/**
	 * Limpia el título y el subtítulo de la pantalla de un jugador y resetea su estado.
	 */
	public static void resetTitle(Player player) {
		if (player == null || !player.isOnline()) {
			return;
		}

		playerTitleStates.remove(player.getUniqueId());

		try {
			Method resetTitleMethod = player.getClass().getMethod("resetTitle");
			resetTitleMethod.invoke(player);
		} catch (Exception e) {
			System.err.println("[DBSparking] Failed to reset title via reflection. Is NecroTempus mod installed?");
			e.printStackTrace();
		}
	}
}
