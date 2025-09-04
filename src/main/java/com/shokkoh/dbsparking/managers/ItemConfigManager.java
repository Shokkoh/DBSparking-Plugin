package com.shokkoh.dbsparking.managers;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.items.CustomItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ItemConfigManager {

	private final DBSparking plugin;
	private final File itemsFile;
	private FileConfiguration itemsConfig;
	private final Map<String, CustomItem> customItems = new HashMap<>();
	private final Map<UUID, ItemStack> playersCreatingItem = new HashMap<>();

	public ItemConfigManager(DBSparking plugin) {
		this.plugin = plugin;
		this.itemsFile = new File(plugin.getDataFolder(), "items.yml");
		this.itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
		loadItems();
	}

	public void loadItems() {
		customItems.clear();
		this.itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

		ConfigurationSection itemsSection = itemsConfig.getConfigurationSection("Items");
		if (itemsSection == null) {
			plugin.getLogger().severe("Section 'Items' not found in items.yml! File might be corrupted.");
			return;
		}

		for (String key : itemsSection.getKeys(false)) {
			CustomItem item = new CustomItem();
			item.setInternalName(key);
			item.setItemName(itemsSection.getString(key + ".ItemName"));
			item.setItemId(itemsSection.getInt(key + ".ItemID"));
			item.setItemData(itemsSection.getInt(key + ".ItemData", 0));
			item.setDamage(itemsSection.getDouble(key + ".Damage", -1.0));
			item.setExecuteAsOp(itemsSection.getBoolean(key + ".ExecuteAsOp", false));
			item.setConsumeOnUse(itemsSection.getBoolean(key + ".ConsumeOnUse", true));
			item.setCommands(itemsSection.getStringList(key + ".Commands"));
			item.setLore(itemsSection.getStringList(key + ".Lore"));
			item.setMessageOnUse(itemsSection.getString(key + ".MessageOnUse"));
			customItems.put(key.toLowerCase(), item);
		}
	}

	public void finishItemCreation(Player player, String internalName) {
		ItemStack sourceItem = playersCreatingItem.remove(player.getUniqueId());

		String itemName = sourceItem.hasItemMeta() && sourceItem.getItemMeta().hasDisplayName()
				? sourceItem.getItemMeta().getDisplayName()
				: "&f" + internalName;

		String path = "Items." + internalName;
		itemsConfig.set(path + ".ItemName", itemName);
		itemsConfig.set(path + ".ItemID", sourceItem.getType().getId());
		itemsConfig.set(path + ".ItemData", sourceItem.getData().getData());
		itemsConfig.set(path + ".Damage", -1.0);
		itemsConfig.set(path + ".ExecuteAsOp", false);
		itemsConfig.set(path + ".ConsumeOnUse", true);
		itemsConfig.set(path + ".Commands", Collections.singletonList("say Hola, %player%!"));
		itemsConfig.set(path + ".Lore", Arrays.asList("", "&7Este es un item editable.", ""));
		itemsConfig.set(path + ".MessageOnUse", "&aHas usado el item " + internalName + "!");

		try {
			itemsConfig.save(itemsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadItems();
	}

	public void startItemCreation(Player player) {
		playersCreatingItem.put(player.getUniqueId(), player.getItemInHand().clone());
	}

	public boolean isCreatingItem(Player player) {
		return playersCreatingItem.containsKey(player.getUniqueId());
	}

	public void cancelCreation(Player player) {
		playersCreatingItem.remove(player.getUniqueId());
	}

	public CustomItem getItem(String internalName) {
		return customItems.get(internalName.toLowerCase());
	}

	public List<CustomItem> getAllItems() {
		return customItems.values().stream()
				.sorted(Comparator.comparing(CustomItem::getInternalName))
				.collect(Collectors.toList());
	}
}