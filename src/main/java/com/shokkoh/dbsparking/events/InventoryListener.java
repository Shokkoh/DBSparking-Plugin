package com.shokkoh.dbsparking.events;

import com.shokkoh.dbsparking.inventories.CustomInventory;
import com.shokkoh.dbsparking.inventories.CustomItemGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListener implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		InventoryHolder holder = e.getInventory().getHolder();

		if (holder instanceof CustomInventory) {
			CustomInventory customInventory = (CustomInventory) holder;
			customInventory.handleClick(e);
		}

		if (holder instanceof CustomItemGUI) {
			CustomItemGUI customItemGUI = (CustomItemGUI) holder;
			customItemGUI.handleClick(e);
		}
	}
}
