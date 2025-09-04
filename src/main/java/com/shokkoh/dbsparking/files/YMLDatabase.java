package com.shokkoh.dbsparking.files;

import com.shokkoh.dbsparking.utils.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Gestiona un único archivo YML, asegurando su creación, carga y guardado.
 */
public class YMLDatabase {

	private final JavaPlugin plugin;
	private final String resourcePath;
	private final String fileName;
	private final File file;
	private FileConfiguration config;

	public YMLDatabase(JavaPlugin plugin, String fileName, File folder) {
		this.plugin = plugin;
		this.fileName = fileName;
		this.file = new File(folder, fileName);
		this.resourcePath = folder.getName() + "/" + fileName;
	}

	/**
	 * Registra el archivo. Si no existe en la carpeta del plugin,
	 * lo copia desde los recursos del JAR. Luego, lo carga.
	 */
	public void register() {
		if (!file.exists()) {
			Logger.info("Language file " + file.getName() + " not found, it will be created.");
			return;
		}
		config = YamlConfiguration.loadConfiguration(file);
		reloadDB();
	}

	/**
	 * Guarda la configuración actual en el archivo.
	 */
	public void saveDB() {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			writer.write(config.saveToString());
		} catch (IOException e) {
			Logger.severe("Could not save file " + file.getName());
			e.printStackTrace();
		}
	}

	/**
	 * Obtiene la instancia de FileConfiguration cargada.
	 * @return La configuración del archivo.
	 */
	public FileConfiguration getDB() {
		if (config == null) {
			reloadDB();
		}
		return config;
	}

	/**
	 * Recarga la configuración desde el archivo en el disco y los valores por defecto desde el JAR.
	 */
	public void reloadDB() {
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			config = YamlConfiguration.loadConfiguration(reader);
		} catch (IOException e) {
			Logger.severe("Could not load configuration from " + file.getName());
			e.printStackTrace();
			config = new YamlConfiguration();
		}

		InputStream defConfigFileStream = plugin.getResource(resourcePath);
		if (defConfigFileStream != null) {
			config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigFileStream, StandardCharsets.UTF_8)));
		}
	}
}