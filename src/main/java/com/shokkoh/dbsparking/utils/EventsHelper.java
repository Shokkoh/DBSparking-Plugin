package com.shokkoh.dbsparking.utils;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.items.StatsItem;
import com.shokkoh.dbsparking.items.ItemType;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import noppes.npcs.api.entity.IDBCPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class EventsHelper {

	private EventsHelper() {}

	public static final List<String> DEV_UUIDS = Arrays.asList(
			"f4353548-7e4d-3969-a6de-5b32aba10ae2", // ezShokkoh - Offline
			"19e318eb-9131-4466-af50-4958348249b8", // ezShokkoh - Online
			"28f090a4-e2b9-3382-a2d8-c15109ba57d1", // ImYuseix - Offline
			"5d651997-3ea5-49f3-8033-4ddce9cf8f4e"  // ImYuseix - Online
	);

	/**
	 * Maneja la lógica cuando un jugador equipa un ítem.
	 * @param plugin La instancia principal del plugin.
	 * @param player El jugador.
	 * @param item El ítem equipado.
	 * @param isArmorSlot Indica si el ítem fue equipado en un slot de armadura.
	 */
	public static void handleItemEquip(DBSparking plugin, Player player, ItemStack item, boolean isArmorSlot) {
		String internalName = NBTEditor.getItemTag(item);
		if (internalName == null) return;
		StatsItem statsItem = plugin.getItemDataManager().getItemByName(internalName);
		if (statsItem == null) return;

		if (statsItem.getItemType() == ItemType.ARMOR && !isArmorSlot) return;
		if (statsItem.getItemType() == ItemType.WEAPON && isArmorSlot) return;
		if (statsItem.getItemType() == ItemType.SOUL) return;

		if (Permissions.ITEM_CONDITION_OVERRIDE.has(player)) {
			plugin.getItemManager().applyItemStats(player, statsItem);
			return;
		}

		String requiredRank = statsItem.getRequiredRank();
		if (requiredRank != null && !requiredRank.isEmpty()) {
			boolean hasRank = false;
			String rankDisplayName = requiredRank;

			if (DBSparking.luckPerms != null) {
				hasRank = player.hasPermission("group." + requiredRank);
				Group group = DBSparking.luckPerms.getGroupManager().getGroup(requiredRank);
				if (group != null) {
					int highestWeight = -1;
					String bestPrefix = null;

					for (Node node : group.getNodes()) {
						String key = node.getKey();
						if (key.startsWith("prefix.")) {
							String[] parts = key.split("\\.");
							if (parts.length >= 3) {
								try {
									int weight = Integer.parseInt(parts[1]);
									if (weight > highestWeight) {
										highestWeight = weight;
										StringBuilder prefixBuilder = new StringBuilder();
										for (int i = 2; i < parts.length; i++) {
											prefixBuilder.append(parts[i]);
											if (i < parts.length - 1) {
												prefixBuilder.append(".");
											}
										}
										bestPrefix = prefixBuilder.toString();
									}
								} catch (NumberFormatException ignored) {
								}
							}
						}
					}

					if (bestPrefix != null) {
						rankDisplayName = bestPrefix;
					} else if (group.getDisplayName() != null) {
						rankDisplayName = group.getDisplayName();
					}
				}
			} else if (DBSparking.perms != null) {
				hasRank = DBSparking.perms.playerInGroup(player, requiredRank);
			}

			if (!hasRank) {
				player.sendMessage(plugin.getLanguage().getMessage("item_condition_rank_fail").replace("%rank%", Text.color(rankDisplayName)));
				return;
			}
		}

		int minLevel = statsItem.getMinLevel();
		int maxLevel = statsItem.getMaxLevel();
		if (minLevel > 0 || maxLevel != -1) {
			IDBCPlayer dbcPlayer = plugin.getDbcManager().getDBC(player);
			if (dbcPlayer != null) {
				int playerLevel = ((dbcPlayer.getStat("str") + dbcPlayer.getStat("dex") + dbcPlayer.getStat("con") +
						dbcPlayer.getStat("wil") + dbcPlayer.getStat("mnd") + dbcPlayer.getStat("spi")) / 5 - 11);

				if (playerLevel < minLevel || (maxLevel != -1 && playerLevel > maxLevel)) {
					String min = String.valueOf(minLevel);
					String max = (maxLevel == -1) ? "Any" : String.valueOf(maxLevel);
					player.sendMessage(plugin.getLanguage().getMessage("item_condition_level_fail").replace("%min%", min).replace("%max%", max));
					return;
				}
			}
		}


		plugin.getItemManager().applyItemStats(player, statsItem);
	}

	/**
	 * Maneja la lógica cuando un jugador desequipa un ítem.
	 * @param plugin La instancia principal del plugin.
	 * @param player El jugador.
	 * @param item El ítem desequipado.
	 */
	public static void handleItemUnequip(DBSparking plugin, Player player, ItemStack item) {
		String internalName = NBTEditor.getItemTag(item);
		if (internalName != null) {
			StatsItem statsItem = plugin.getItemDataManager().getItemByName(internalName);
			if (statsItem != null) {
				plugin.getItemManager().removeItemStats(player, statsItem);
			}
		}
	}

	/**
	 * Comprueba si un jugador es un desarrollador del plugin.
	 * @param playerUUID El UUID del jugador a comprobar.
	 * @return true si es un desarrollador, false si no.
	 */
	public static boolean isDev(UUID playerUUID) {
		return DEV_UUIDS.contains(playerUUID.toString());
	}
}