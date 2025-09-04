package com.shokkoh.dbsparking.inventories;

import com.shokkoh.dbsparking.DBSparking;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class CustomInventory implements InventoryHolder {
	protected final DBSparking plugin = DBSparking.getInstance();
	protected final Player player;
	protected Inventory inventory;

	public CustomInventory(Player player) {
		this.player = player;
	}

	public abstract String getTitle();
	public abstract int getSize();
	public abstract void setupItems();

	public abstract void handleClick(InventoryClickEvent event);

	public void open() {
		inventory = Bukkit.createInventory(this, getSize(), getTitle());
		this.setupItems();
		player.openInventory(inventory);
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}
}
