package com.shokkoh.dbsparking;

import com.shokkoh.dbsparking.boosters.BoostsTask;
import com.shokkoh.dbsparking.commands.CommandManager;
import com.shokkoh.dbsparking.database.BoostDataManager;
import com.shokkoh.dbsparking.database.DatabaseManager;
import com.shokkoh.dbsparking.database.ItemDataManager;
import com.shokkoh.dbsparking.events.InventoryListener;
import com.shokkoh.dbsparking.events.PlayerListener;
import com.shokkoh.dbsparking.files.Language;
import com.shokkoh.dbsparking.managers.*;
import com.shokkoh.dbsparking.tasks.ItemEquipTask;
import com.shokkoh.dbsparking.tasks.TpsTask;
import com.shokkoh.dbsparking.utils.DBSPlaceholders;
import com.shokkoh.dbsparking.utils.Logger;
import com.shokkoh.dbsparking.utils.WorldGuardBridge;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class DBSparking extends JavaPlugin {
    private static DBSparking instance;

    private Language language;
    private PlayerManager playerManager;
    private DBCManager dbcManager;
    private DatabaseManager databaseManager;
    private BoostManager boostManager;
    private BoostDataManager boostDataManager;
    private ItemManager itemManager;
    private ItemDataManager itemDataManager;
    private ItemEquipTask itemEquipTask;
    private DataFetcherManager dataFetcherManager;
    private ItemConfigManager itemConfigManager;
    private AutoLoginManager autoLoginManager;
    public static Permission perms = null;
    public static LuckPerms luckPerms = null;
    private WorldGuardBridge wgBridge;

    PluginDescriptionFile pdfile = this.getDescription();
    public String PlVersion;
    public String PlName;

    public DBSparking() {
        this.PlVersion = this.pdfile.getVersion();
        this.PlName = "&eD&cB&bSparking!";
    }

    @Override
    public void onEnable() {
        instance = this;

        Logger.raw("&e< &6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= &e>");
        Logger.raw("&aEnabling " + PlName + " v" + pdfile.getVersion());
        Logger.raw("&aDeveloped by &eShokkoh&a.");
        Logger.raw("");

        // 1. Cargar configuración y archivos de idioma
        saveDefaultConfig();
        this.language = new Language(this);
        saveDefaultItemsFile();
        saveDefaultAutoLoginFile();

        // 2. Conectar a la base de datos
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initializeTables();

        // 3. Conectar a plugins externos (Hooks)
        if (!setupPermissions()) {
            Logger.severe("Vault/LuckPerms not found! Rank boosts will not work.");
        } else {
            Logger.info("Successfully hooked into a permissions plugin.");
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new DBSPlaceholders(this).register();
            Logger.info("Successfully hooked into PlaceholderAPI.");
        } else {
            Logger.warning("PlaceholderAPI not found, placeholders will not work.");
        }

        // 4. Inicializar los Managers
        this.playerManager = new PlayerManager();
        this.dbcManager = new DBCManager();
        this.boostDataManager = new BoostDataManager(this);
        this.boostManager = new BoostManager(this);
        this.itemDataManager = new ItemDataManager(this);
        this.itemManager = new ItemManager(this);
        this.itemConfigManager = new ItemConfigManager(this);
        this.autoLoginManager = new AutoLoginManager(this);
        this.dataFetcherManager = new DataFetcherManager();
        this.wgBridge = new WorldGuardBridge();
        if (wgBridge.isEnabled()) {
            Logger.info("Successfully hooked into WorldGuard.");
        }

        // 5. Cargar datos desde la base de datos a la caché
        this.boostManager.loadBoostsFromDatabase();

        // 6. Registrar Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);

        // 7. Registrar Comandos
        getCommand("dbsp").setExecutor(new CommandManager());

        // 8. Iniciar Tareas
        new BoostsTask().runTaskTimerAsynchronously(this, 20L, 20L);
        this.itemEquipTask = new ItemEquipTask(this);
        this.itemEquipTask.runTaskTimer(this, 100L, 100L);
        new TpsTask(this).runTaskTimerAsynchronously(this, 20L, 20L);

        Logger.raw("");
        Logger.raw("&a Plugin enabled successfully!");
        Logger.raw("&e< &6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= &e>");
    }

    @Override
    public void onDisable() {
        databaseManager.disconnect();
        Logger.raw("&e< &6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= &e>");
        Logger.info("Plugin disabled. Goodbye!");
        Logger.raw("&e< &6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= &e>");
    }

    private boolean setupPermissions() {
        // Prioridad 1: Intentar conectarse a LuckPerms
        if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                luckPerms = provider.getProvider();
                Logger.info("Successfully hooked into LuckPerms.");
                return true;
            }
        }

        // Prioridad 2: Si LuckPerms falla o no existe, intentar con Vault
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            if (rsp != null) {
                perms = rsp.getProvider();
                if (perms != null) {
                    Logger.info("Successfully hooked into Vault as a fallback.");
                    return true;
                }
            }
        }

        return false;
    }

    public static DBSparking getInstance() {
        return instance;
    }

    public Language getLanguage() {
        return language;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public DBCManager getDbcManager() {
        return dbcManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }

    public BoostDataManager getBoostDataManager() {
        return boostDataManager;
    }

	public ItemManager getItemManager() {
		return itemManager;
	}

    public DataFetcherManager getDataFetcherManager() {
        return dataFetcherManager;
    }

    public ItemEquipTask getItemEquipTask() {
        return itemEquipTask;
    }

	public ItemDataManager getItemDataManager() {
		return itemDataManager;
	}

	public ItemConfigManager getItemConfigManager() {
		return itemConfigManager;
	}

    public WorldGuardBridge getWgBridge() {
        return wgBridge;
    }

    public void saveDefaultItemsFile() {
        if (!new File(getDataFolder(), "items.yml").exists()) {
            this.saveResource("items.yml", false);
        }
    }

    public void saveDefaultAutoLoginFile() {
        if (!new File(getDataFolder(), "autologin.yml").exists()) {
            this.saveResource("autologin.yml", false);
        }
    }

	public AutoLoginManager getAutoLoginManager() {
		return autoLoginManager;
	}
}
