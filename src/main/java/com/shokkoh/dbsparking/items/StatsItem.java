package com.shokkoh.dbsparking.items;

import com.shokkoh.dbsparking.DBSparking;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class StatsItem {
	private int id;
	private String internalName;
	private ItemType itemType;
	private String displayName;
	private Material material;
	private int itemData;
	private int minLevel;
	private int maxLevel;
	private String requiredRank;
	private Rarity rarity;

	private List<String> customLore;
	private List<StatBonus> statBonuses;
	private List<String> finalLore;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getInternalName() {
		return internalName;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public ItemType getItemType() {
		return itemType;
	}

	public void setItemType(ItemType itemType) {
		this.itemType = itemType;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public int getItemData() {
		return itemData;
	}

	public void setItemData(int itemData) {
		this.itemData = itemData;
	}

	public List<String> getCustomLore() {
		return customLore;
	}

	public void setCustomLore(List<String> customLore) {
		this.customLore = customLore;
	}

	public List<StatBonus> getStatBonuses() {
		return statBonuses;
	}

	public void setStatBonuses(List<StatBonus> statBonuses) {
		this.statBonuses = statBonuses;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	public String getRequiredRank() {
		return requiredRank;
	}

	public void setRequiredRank(String requiredRank) {
		this.requiredRank = requiredRank;
	}

	public Rarity getRarity() {
		return rarity;
	}

	public void setRarity(Rarity rarity) {
		this.rarity = rarity;
	}

	// AUX:

	public String getRarityName() {
		return DBSparking.getInstance().getLanguage().getRawMessage("item_rarities." + getRarity().name().toLowerCase());
	}

	public String getItemTypeName() {
		if (itemType == null) return "UNKNOWN";
		return itemType.name();
	}

	public ItemStack toItemStack() {
		return new ItemStack(material, 1, (short) itemData);
	}

	public List<String> getFinalLore() {
		return finalLore;
	}

	public void setFinalLore(List<String> finalLore) {
		this.finalLore = finalLore;
	}
}
