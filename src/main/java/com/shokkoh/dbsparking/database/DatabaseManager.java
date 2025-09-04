package com.shokkoh.dbsparking.database;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.utils.Logger;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

	private final DBSparking plugin;
	private Connection connection;
	private final String type;

	public DatabaseManager(DBSparking plugin) {
		this.plugin = plugin;
		this.type = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
		connect();
	}

	private void connect() {
		try {
			if (type.equals("mysql")) {
				FileConfiguration config = plugin.getConfig();
				String host = config.getString("database.mysql.host");
				int port = config.getInt("database.mysql.port");
				String dbName = config.getString("database.mysql.database");
				String user = config.getString("database.mysql.username");
				String pass = config.getString("database.mysql.password");
				boolean useSSL = config.getBoolean("database.mysql.useSSL");

				synchronized (this) {
					Class.forName("com.mysql.jdbc.Driver");
					String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=" + useSSL;
					this.connection = DriverManager.getConnection(url, user, pass);
					Logger.info("Successfully connected to MySQL database.");
				}
			} else {
				File dbFile = new File(plugin.getDataFolder(), "database.db");
				if (!dbFile.exists()) {
					try {
						dbFile.createNewFile();
					} catch (IOException e) {
						Logger.severe("Could not create SQLite database file!");
					}
				}
				synchronized (this) {
					Class.forName("org.sqlite.JDBC");
					this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
					Logger.info("Successfully connected to SQLite database.");
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			Logger.severe("Could not connect to the database! Disabling plugin.");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}
	}

	public void disconnect() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			Logger.severe("Error while disconnecting from the database.");
		}
		Logger.info("Database connection closed successfully.");
	}

	public Connection getConnection() {
		return connection;
	}

	/**
	 * Crea todas las tablas necesarias si no existen.
	 */
	public void initializeTables() {
		// Esta tabla almacenará la información básica de cada jugador.
		String playersTable = "CREATE TABLE IF NOT EXISTS dbs_players ("
				+ "uuid VARCHAR(36) PRIMARY KEY NOT NULL,"
				+ "username VARCHAR(16) NOT NULL,"
				+ "notify_enabled BOOLEAN DEFAULT true,"
				+ "unlocked_soul_slots VARCHAR(255) DEFAULT ''"
				+ ");";


		Logger.info("Player database connection established successfully.");

		// Esta tabla almacenará todas las instancias de boosts activos.
		String activeBoostsTable = "CREATE TABLE IF NOT EXISTS dbs_active_boosts ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "name VARCHAR(45) NOT NULL,"
				+ "boost_type VARCHAR(16) NOT NULL,"
				+ "target VARCHAR(255),"
				+ "target_name VARCHAR(16),"
				+ "amount DOUBLE NOT NULL,"
				+ "duration BIGINT NOT NULL,"
				+ "last_updated TIMESTAMP NOT NULL,"
				+ "author VARCHAR(32)"
				+ ");";

		Logger.info("Boosts database connection established successfully.");

		try (Statement statement = connection.createStatement()) {
			statement.execute(playersTable);
			statement.execute(activeBoostsTable);
			// Tabla principal de ítems
			String itemsTable = "CREATE TABLE IF NOT EXISTS dbs_items ("
					+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "internal_name VARCHAR(45) NOT NULL UNIQUE,"
					+ "item_type VARCHAR(16) NOT NULL,"
					+ "display_name VARCHAR(255) NOT NULL,"
					+ "material VARCHAR(45) NOT NULL,"
					+ "item_data INTEGER DEFAULT 0,"
					+ "min_level INTEGER DEFAULT 0,"
					+ "max_level INTEGER DEFAULT -1,"
					+ "required_rank VARCHAR(255) DEFAULT NULL,"
					+ "rarity VARCHAR(32) DEFAULT 'COMMON'"
					+ ");";
			statement.execute(itemsTable);

			// Tabla para los bonus de estadísticas de cada ítem
			String itemStatsTable = "CREATE TABLE IF NOT EXISTS dbs_item_stats ("
					+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "item_id INTEGER NOT NULL,"
					+ "bonus_id VARCHAR(255) NOT NULL,"
					+ "stat_type VARCHAR(8) NOT NULL,"
					+ "modifier_type VARCHAR(16) NOT NULL,"
					+ "value DOUBLE NOT NULL,"
					+ "FOREIGN KEY(item_id) REFERENCES dbs_items(id) ON DELETE CASCADE"
					+ ");";
			statement.execute(itemStatsTable);

			// Tabla para las Almas Equipadas
			String equippedSoulsTable = "CREATE TABLE IF NOT EXISTS dbs_equipped_souls ("
					+ "uuid VARCHAR(36) NOT NULL,"
					+ "slot INTEGER NOT NULL,"
					+ "item_internal_name VARCHAR(45) NOT NULL,"
					+ "PRIMARY KEY (uuid, slot)"
					+ ");";
			statement.execute(equippedSoulsTable);

			// Tabla para las líneas de lore personalizadas
			String itemLoreTable = "CREATE TABLE IF NOT EXISTS dbs_item_lore ("
					+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "item_id INTEGER NOT NULL,"
					+ "line_number INTEGER NOT NULL,"
					+ "line_text TEXT NOT NULL,"
					+ "FOREIGN KEY(item_id) REFERENCES dbs_items(id) ON DELETE CASCADE"
					+ ");";
			statement.execute(itemLoreTable);

			String itemFinalLoreTable = "CREATE TABLE IF NOT EXISTS dbs_item_final_lore ("
					+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "item_id INTEGER NOT NULL,"
					+ "line_order INTEGER NOT NULL,"
					+ "line_text TEXT NOT NULL,"
					+ "FOREIGN KEY(item_id) REFERENCES dbs_items(id) ON DELETE CASCADE"
					+ ");";
			statement.execute(itemFinalLoreTable);

			Logger.info("Item database connection established successfully.");
			Logger.info("Database tables verified/created successfully.");
		} catch (SQLException e) {
			Logger.severe("Could not create database tables!");
		}
	}
}