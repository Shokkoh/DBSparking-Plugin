package com.shokkoh.dbsparking.managers;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.datafetcher.AutoLoginData;
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
	private final Map<UUID, AutoLoginData> autoLoginData = new HashMap<>();

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
				boolean kick = playersSection.getBoolean(uuidString + ".kickOtherIP", false);

				if (ip != null && !ip.isEmpty()) {
					autoLoginData.put(playerUUID, new AutoLoginData(ip, kick));
				}
			} catch (IllegalArgumentException e) {
				plugin.getLogger().warning("Invalid UUID found on autologin.yml: " + uuidString);
			}
		}
	}

	/**
	 * Obtiene la entrada de autologin completa para un jugador.
	 * @param uuid El UUID del jugador.
	 * @return El objeto AutoLoginData o null si no existe.
	 */
	public AutoLoginData getAutoLoginData(UUID uuid) {
		return autoLoginData.get(uuid);
	}

	/**
	 * Vincula la IP actual de un jugador a su UUID para el autologin.
	 * @param player El jugador que activa el autologin.
	 */
	public void setAutoLogin(Player player) {
		String currentIp = player.getAddress().getAddress().getHostAddress();
		UUID playerUUID = player.getUniqueId();

		autoLoginData.put(playerUUID, new AutoLoginData(currentIp, false));

		String path = "players." + playerUUID.toString();
		autoLoginConfig.set(path + ".playerName", player.getName());
		autoLoginConfig.set(path + ".ip", currentIp);
		autoLoginConfig.set(path + ".kickOtherIP", false);

		try {
			autoLoginConfig.save(autoLoginFile);
		} catch (IOException e) {
			plugin.getLogger().severe("No se pudo guardar el archivo autologin.yml!");
			e.printStackTrace();
		}
	}
}