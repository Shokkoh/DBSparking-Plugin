package com.shokkoh.dbsparking.managers;

import com.shokkoh.dbsparking.DBSparking;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AutoLoginManager {

	private final DBSparking plugin;
	private final File autoLoginFile;
	private FileConfiguration autoLoginConfig;
	private final Map<UUID, String> autoLoginData = new HashMap<>();

	public AutoLoginManager(DBSparking plugin) {
		this.plugin = plugin;
		this.autoLoginFile = new File(plugin.getDataFolder(), "autologin.yml");
		if (!autoLoginFile.exists()) {
			plugin.saveResource("autologin.yml", false);
		}
		load();
	}

	/**
	 * Carga o recarga los datos desde autologin.yml a la memoria.
	 */
	public void load() {
		autoLoginData.clear();
		autoLoginConfig = YamlConfiguration.loadConfiguration(autoLoginFile);
		ConfigurationSection playersSection = autoLoginConfig.getConfigurationSection("players");
		if (playersSection == null) {
			return;
		}

		for (String uuidString : playersSection.getKeys(false)) {
			try {
				UUID playerUUID = UUID.fromString(uuidString);
				String ip = playersSection.getString(uuidString + ".ip");
				if (ip != null && !ip.isEmpty()) {
					autoLoginData.put(playerUUID, ip);
				}
			} catch (IllegalArgumentException e) {
				plugin.getLogger().warning("Invalid UUID found on autologin.yml: " + uuidString);
			}
		}
	}

	/**
	 * Comprueba si un jugador debe ser autenticado automáticamente.
	 * @param player El jugador que se conecta.
	 * @return true si el UUID y la IP coinciden con los datos guardados.
	 */
	public boolean shouldAutoLogin(Player player) {
		String registeredIp = autoLoginData.get(player.getUniqueId());
		if (registeredIp == null) {
			return false;
		}
		String currentIp = player.getAddress().getAddress().getHostAddress();
		return registeredIp.equals(currentIp);
	}

	/**
	 * Vincula la IP actual de un jugador a su UUID para el autologin.
	 * @param player El jugador que activa el autologin.
	 */
	public void setAutoLogin(Player player) {
		String currentIp = player.getAddress().getAddress().getHostAddress();
		UUID playerUUID = player.getUniqueId();

		autoLoginData.put(playerUUID, currentIp);

		String path = "players." + playerUUID.toString();
		autoLoginConfig.set(path + ".playerName", player.getName());
		autoLoginConfig.set(path + ".ip", currentIp);

		try {
			autoLoginConfig.save(autoLoginFile);
		} catch (IOException e) {
			plugin.getLogger().severe("autologin.yml could not be saved!");
			e.printStackTrace();
		}
	}
}