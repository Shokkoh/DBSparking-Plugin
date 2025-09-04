package com.shokkoh.dbsparking.tasks;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.utils.EventsHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemEquipTask extends BukkitRunnable {

	private final DBSparking plugin;
	private final Map<UUID, ItemStack[]> lastArmor = new HashMap<>();

	public ItemEquipTask(DBSparking plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			ItemStack[] currentArmor = player.getInventory().getArmorContents();
			ItemStack[] previousArmor = lastArmor.get(player.getUniqueId());

			if (previousArmor == null) {
				lastArmor.put(player.getUniqueId(), cloneItemStacks(currentArmor));
				for (ItemStack item : currentArmor) {
					EventsHelper.handleItemEquip(plugin, player, item, true);
				}
				continue;
			}

			for (int i = 0; i < currentArmor.length; i++) {
				ItemStack newItem = currentArmor[i];
				ItemStack oldItem = previousArmor[i];

				if (!areItemsSimilar(newItem, oldItem)) {
					EventsHelper.handleItemUnequip(plugin, player, oldItem);
					EventsHelper.handleItemEquip(plugin, player, newItem, true);
				}
			}
			lastArmor.put(player.getUniqueId(), cloneItemStacks(currentArmor));
		}
	}

	public void playerQuit(Player player) {
		ItemStack[] armor = lastArmor.remove(player.getUniqueId());
		if (armor != null) {
			for (ItemStack item : armor) {
				EventsHelper.handleItemUnequip(plugin, player, item);
			}
		}
	}

	private ItemStack[] cloneItemStacks(ItemStack[] array) {
		ItemStack[] newArray = new ItemStack[array.length];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = array[i] != null ? array[i].clone() : null;
		}
		return newArray;
	}

	private boolean areItemsSimilar(ItemStack a, ItemStack b) {
		if (a == null && b == null) return true;
		if (a == null || b == null) return false;
		return a.isSimilar(b);
	}
}