package com.shokkoh.dbsparking.items;

import java.util.List;

public class CustomItem {

	private String internalName;
	private String itemName;
	private int itemId;
	private int itemData;
	private double damage;
	private boolean executeAsOp;
	private boolean consumeOnUse;
	private List<String> commands;
	private List<String> lore;
	private String messageOnUse;

	public String getInternalName() {
		return internalName;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public boolean isExecuteAsOp() {
		return executeAsOp;
	}

	public void setExecuteAsOp(boolean executeAsOp) {
		this.executeAsOp = executeAsOp;
	}

	public boolean isConsumeOnUse() {
		return consumeOnUse;
	}

	public void setConsumeOnUse(boolean consumeOnUse) {
		this.consumeOnUse = consumeOnUse;
	}

	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

	public List<String> getLore() {
		return lore;
	}

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

	public String getMessageOnUse() {
		return messageOnUse;
	}

	public void setMessageOnUse(String messageOnUse) {
		this.messageOnUse = messageOnUse;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getItemData() {
		return itemData;
	}

	public void setItemData(int itemData) {
		this.itemData = itemData;
	}
}