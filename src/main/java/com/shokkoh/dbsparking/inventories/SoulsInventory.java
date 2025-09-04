package com.shokkoh.dbsparking.inventories;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.entities.DBSPlayer;
import com.shokkoh.dbsparking.items.StatsItem;
import com.shokkoh.dbsparking.items.ItemType;
import com.shokkoh.dbsparking.utils.NBTEditor;
import com.shokkoh.dbsparking.utils.NumberFormats;
import com.shokkoh.dbsparking.utils.Text;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SoulsInventory extends CustomInventory {
	private final DBSPlayer dbsPlayer;
	private static final int SOUL_SLOT_1 = 11, SOUL_SLOT_2 = 12, SOUL_SLOT_3 = 14, SOUL_SLOT_4 = 15;

	/**
	 * Constructor para cuando un jugador abre su propio inventario.
	 */
	public SoulsInventory(Player player) {
		this(player, player);
	}

	/**
	 * Constructor para administradores.
	 * @param target El jugador dueño de las almas.
	 * @param viewer El jugador que verá el inventario (el admin).
	 */
	public SoulsInventory(Player target, Player viewer) {
		super(viewer);
		this.dbsPlayer = DBSparking.getInstance().getPlayerManager().getDBSPlayer(target);
	}

	@Override
	public String getTitle() {
		return plugin.getLanguage().getRawMessage("placeholder.soul_inventory_title");
	}

	@Override
	public int getSize() {
		return 27;
	}

	@Override
	public void setupItems() {
		Player targetPlayer = dbsPlayer.getPlayer();
		if (targetPlayer == null || !targetPlayer.isOnline()) {
			return;
		}

		ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
		ItemMeta meta = pane.getItemMeta();
		meta.setDisplayName(" ");
		pane.setItemMeta(meta);
		for (int i = 0; i < getSize(); i++) {
			inventory.setItem(i, pane);
		}

		setupSlot(SOUL_SLOT_1, 1, null, 0);

		String rankSlot2 = plugin.getConfig().getString("souls.slot2.required_rank", "renacer2");
		int costSlot2 = plugin.getConfig().getInt("souls.slot2.tp_cost", 1000000000);
		setupSlot(SOUL_SLOT_2, 2, rankSlot2, costSlot2);

		String blockedSlot = plugin.getLanguage().getRawMessage("soul_blocked_slot");
		String groupName = "";

		boolean isVipOrHigher = (DBSparking.perms != null && (DBSparking.perms.playerInGroup(player, "vip") || DBSparking.perms.playerInGroup(player, "vip2"))
		|| player.hasPermission("group.vip") || player.hasPermission("group.vip2"));
		if (isVipOrHigher) {
			setupSlot(SOUL_SLOT_3, 3, null, 0);
		} else {
			groupName = "VIP";
			inventory.setItem(SOUL_SLOT_3, createLockedItem(blockedSlot, Collections.singletonList(plugin.getLanguage().getRawMessage("soul_required_group").replace("%group%", groupName))));
		}

		if (DBSparking.perms != null && DBSparking.perms.playerInGroup(player, "vip2") || player.hasPermission("group.vip2")) {
			int costSlot4 = plugin.getConfig().getInt("souls_inventory.slot4.tp_cost");
			setupSlot(SOUL_SLOT_4, 4, null, costSlot4);
		} else {
			groupName = "VIP+";
			inventory.setItem(SOUL_SLOT_4, createLockedItem(blockedSlot, Collections.singletonList(plugin.getLanguage().getRawMessage("soul_required_group").replace("%group%", groupName))));
		}
	}

	private void setupSlot(int inventorySlot, int soulSlot, String requiredRank, int tpCost) {
		Player targetPlayer = dbsPlayer.getPlayer();

		StatsItem equippedSoul = dbsPlayer.getEquippedSoul(soulSlot);
		if (equippedSoul != null) {
			inventory.setItem(inventorySlot, createSoulItemStack(equippedSoul));
			return;
		}

		boolean hasRank = (requiredRank == null || player.hasPermission("group." + requiredRank));
		String blockedSlot = plugin.getLanguage().getRawMessage("soul_blocked_slot");

		if (soulSlot == 3) {
			hasRank = targetPlayer.hasPermission("group.vip") || targetPlayer.hasPermission("group.vip2");
		}

		if (!hasRank) {
			inventory.setItem(inventorySlot, createLockedItem(blockedSlot, Collections.singletonList(plugin.getLanguage().getRawMessage("soul_required_group").replace("%group%", requiredRank))));
			return;
		}

		if (tpCost > 0 && !dbsPlayer.getUnlockedSoulSlots().contains(soulSlot)) {
			String unlockCost = plugin.getLanguage().getRawMessage("soul_unlock_cost").replace("%cost%", NumberFormats.formatInt(tpCost));
			String clickUnlock = plugin.getLanguage().getRawMessage("soul_unlock_click");
			ItemStack buyItem = new ItemStack(Material.WOOL, 1, (short) 4);
			ItemMeta meta = buyItem.getItemMeta();
			meta.setDisplayName(plugin.getLanguage().getRawMessage("soul_unlock_slot").replace("%slot%", String.valueOf(soulSlot)));
			meta.setLore(Arrays.asList(unlockCost, clickUnlock));
			buyItem.setItemMeta(meta);
			inventory.setItem(inventorySlot, buyItem);
		} else {
			inventory.setItem(inventorySlot, null);
		}
	}

	@Override
	public void handleClick(InventoryClickEvent event) {
		int slot = event.getRawSlot();
		if (slot < 0) return;

		Player viewer = (Player) event.getWhoClicked();
		Player target = dbsPlayer.getPlayer();

		DBSPlayer targetDbsPlayer = this.dbsPlayer;
		DBSPlayer viewerDbsPlayer = plugin.getPlayerManager().getDBSPlayer(viewer);

		ItemStack clickedItem = event.getCurrentItem();
		boolean isPlayerInventory = event.getRawSlot() >= this.getSize();

		if (clickedItem != null && clickedItem.getType() == Material.WOOL) {
			event.setCancelled(true);
			if (clickedItem.getData().getData() == 4) {
				int soulSlot = 0;
				int cost = 0;
				if (slot == SOUL_SLOT_2) { soulSlot = 2; cost = plugin.getConfig().getInt("souls_inventory.slot2.tp_cost"); }
				if (slot == SOUL_SLOT_4) { soulSlot = 4; cost = plugin.getConfig().getInt("souls_inventory.slot4.tp_cost"); }

				if (soulSlot != 0) {
					int playerTPs = plugin.getDbcManager().getTP(target);
					if (playerTPs >= cost) {
						plugin.getDbcManager().setTP(target, playerTPs - cost);
						plugin.getPlayerManager().unlockSlot(targetDbsPlayer, soulSlot);
						viewer.sendMessage(plugin.getLanguage().getMessage("soul_unlocked").replace("%slot%", String.valueOf(soulSlot)));
						viewer.playSound(viewer.getLocation(), Sound.NOTE_PIANO, 1.0f, 1.0f);
						setupItems();
					} else {
						viewer.sendMessage(plugin.getLanguage().getMessage("soul_insufficient_tps").replace("%cost%", NumberFormats.formatInt(cost)).replace("%current%", String.valueOf(cost)));
						viewer.playSound(viewer.getLocation(), Sound.ANVIL_BREAK, 1.0f, 1.0f);
					}
				}
			}
			return;
		}

		if (isPlayerInventory && isItemASoul(clickedItem)) {
			event.setCancelled(true);
			StatsItem soulToEquip = plugin.getItemDataManager().getItemByName(NBTEditor.getItemTag(clickedItem));

			for (int i = 1; i <= 4; i++) {

				if (dbsPlayer.getEquippedSoul(i) == null && dbsPlayer.getUnlockedSoulSlots().contains(i)) {
					dbsPlayer.setEquippedSoul(i, soulToEquip);
					if (clickedItem.getAmount() > 1) {
						clickedItem.setAmount(clickedItem.getAmount() - 1);
						viewer.getInventory().setItem(event.getSlot(), clickedItem);
					} else {
						viewer.getInventory().setItem(event.getSlot(), null);
					}
					viewer.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.2f);
					setupItems();
					return;
				}
			}
			viewer.sendMessage(plugin.getLanguage().getMessage("soul_no_empty_slot"));
			return;
		}

		int clickedSoulSlot = getSoulSlotFromInventorySlot(event.getRawSlot());
		if (clickedSoulSlot != 0 && clickedItem != null && clickedItem.getType() != Material.AIR) {
			event.setCancelled(true);

			if (viewer.getInventory().firstEmpty() == -1) {
				viewer.sendMessage(plugin.getLanguage().getMessage("soul_inventory_full"));
				return;
			}

			StatsItem soulToUnequip = targetDbsPlayer.getEquippedSoul(clickedSoulSlot);
			if (soulToUnequip != null) {
				targetDbsPlayer.setEquippedSoul(clickedSoulSlot, null);
				targetDbsPlayer.giveCustomItem(soulToUnequip.getInternalName());
				viewer.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0f, 1.0f);
				setupItems();
			}
			return;
		}

		event.setCancelled(true);
	}

	private ItemStack createLockedItem(String name, List<String> lore) {
		ItemStack lockedSlot = new ItemStack(Material.WOOL, 1, (short) 14);
		ItemMeta meta = lockedSlot.getItemMeta();
		meta.setDisplayName(Text.color(name));
		meta.setLore(lore);
		lockedSlot.setItemMeta(meta);
		return lockedSlot;
	}

	private ItemStack createSoulItemStack(StatsItem soul) {
		ItemStack itemStack = new ItemStack(soul.getMaterial(), 1, (short) soul.getItemData());
		ItemMeta meta = itemStack.getItemMeta();

		meta.setDisplayName(Text.color(soul.getDisplayName()));
		List<String> finalLore = new ArrayList<>();

		if (soul.getCustomLore() != null && !soul.getCustomLore().isEmpty()) {
			for (String line : soul.getCustomLore()) {
				finalLore.add(Text.color(line));
			}
		}

		List<String> statLore = plugin.getItemDataManager().getStatLoreFor(soul);
		if (!statLore.isEmpty()) {
			if (!finalLore.isEmpty()) {
				finalLore.add("");
			}
			finalLore.addAll(statLore);
		}

		if (soul.getRarity() != null) {
			if (!finalLore.isEmpty()) {
				finalLore.add("");
			}
			String rarityPath = "item_rarities." + soul.getRarity().name().toLowerCase();
			finalLore.add(plugin.getLanguage().getRawMessage(rarityPath));
		}

		if (!finalLore.isEmpty()) {
			meta.setLore(finalLore);
		}

		itemStack.setItemMeta(meta);

		return NBTEditor.setItemTag(itemStack, soul.getInternalName());
	}

	/**
	 * Métodos de ayuda para comprobar si un ItemStack es un Alma.
	 */
	private boolean isItemASoul(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) return false;
		String tag = NBTEditor.getItemTag(item);
		if (tag == null) return false;
		StatsItem statsItem = plugin.getItemDataManager().getItemByName(tag);
		return statsItem != null && statsItem.getItemType() == ItemType.SOUL;
	}

	/**
	 * Devuelve el número de slot lógico (1-4) a partir del slot del inventario.
	 */
	private int getSoulSlotFromInventorySlot(int inventorySlot) {
		switch (inventorySlot) {
			case SOUL_SLOT_1: return 1;
			case SOUL_SLOT_2: return 2;
			case SOUL_SLOT_3: return 3;
			case SOUL_SLOT_4: return 4;
			default: return 0;
		}
	}
}
