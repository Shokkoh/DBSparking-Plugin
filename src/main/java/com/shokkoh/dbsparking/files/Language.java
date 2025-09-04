package com.shokkoh.dbsparking.files;

import com.shokkoh.dbsparking.utils.Text;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * Administra los archivos de idioma utilizando YMLDatabase.
 * Carga el idioma especificado en la config.yml principal.
 */
public class Language {

	private final JavaPlugin plugin;
	private final File langFolder;
	private YMLDatabase languageDB;
	private String selectedLanguage;

	public Language(JavaPlugin plugin) {
		this.plugin = plugin;
		this.langFolder = new File(plugin.getDataFolder(), "lang");

		if (!langFolder.exists()) {
			langFolder.mkdirs();
		}

		registerDefaultLanguages();
		reload();
	}

	/**
	 * Recarga el archivo de idioma basándose en la config.yml.
	 */
	public void reload() {
		this.selectedLanguage = plugin.getConfig().getString("language", "en");
		String langFileName = selectedLanguage + ".yml";

		File langFile = new File(langFolder, langFileName);
		if(!langFile.exists()){
			copyResource("lang/" + langFileName, langFile);
		}

		this.languageDB = new YMLDatabase(plugin, langFileName, langFolder);
		this.languageDB.register();
	}

	/**
	 * Obtiene la configuración del archivo de idioma cargado.
	 * @return FileConfiguration del idioma seleccionado.
	 */
	public FileConfiguration getLangFile() {
		return languageDB.getDB();
	}

	/**
	 * Obtiene un mensaje de la configuración y le aplica colores.
	 * @param path La ruta del mensaje (ej. "no_permission").
	 * @return El mensaje formateado.
	 */
	public String getMessage(String path) {
		String prefix = getLangFile().getString("prefix", "");
		String message = getLangFile().getString(path, "&cMessage not found: " + path);
		return Text.color(prefix + message);
	}

	/**
	 * Obtiene un mensaje sin prefijo, útil para placeholders o títulos.
	 * @param path La ruta del mensaje (ej. "boost.placeholder.inactive").
	 * @return El mensaje formateado sin prefijo.
	 */
	public String getRawMessage(String path) {
		String message = getLangFile().getString(path, "&cMessage not found: " + path);
		return Text.color(message);
	}

	private void registerDefaultLanguages() {
		String[] langs = {"en", "es"};
		for (String lang : langs) {
			File targetFile = new File(langFolder, lang + ".yml");
			if (!targetFile.exists()) {
				copyResource("lang/" + lang + ".yml", targetFile);
			}
		}
	}

	private void copyResource(String resourcePath, File targetFile) {
		try (InputStream in = plugin.getResource(resourcePath)) {
			if (in == null) {
				plugin.getLogger().warning("Could not find language resource: " + resourcePath);
				return;
			}
			Files.copy(in, targetFile.toPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Obtiene una lista de strings de la configuración y le aplica colores a cada línea.
	 * @param path La ruta de la lista de strings (ej. "autologin_confirm").
	 * @return La lista de mensajes formateados.
	 */
	public List<String> getStringList(String path) {
		List<String> messages = getLangFile().getStringList(path);

		if (messages == null || messages.isEmpty()) {
			return java.util.Collections.singletonList(Text.color("&cMessage list not found: " + path));
		}

		String prefix = getLangFile().getString("prefix", "");

		if (!prefix.isEmpty()) {
			messages.set(0, prefix + messages.get(0));
		}

		messages.replaceAll(Text::color);

		return messages;
	}
}
