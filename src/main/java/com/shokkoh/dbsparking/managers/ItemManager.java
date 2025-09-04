package com.shokkoh.dbsparking.managers;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.items.StatsItem;
import com.shokkoh.dbsparking.items.Stat;
import com.shokkoh.dbsparking.items.StatBonus;
import noppes.npcs.api.entity.IDBCPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemManager {
	private final DBSparking plugin;
	private final Map<UUID, Object[]> playerLoreEditing = new HashMap<>();
	private final Map<UUID, Object[]> finalLoreEditing = new HashMap<>();

	public ItemManager(DBSparking plugin) {
		this.plugin = plugin;
	}

	/**
	 * Aplica los bonus de un ítem a un jugador.
	 * @param player El jugador.
	 * @param item El StatsItem a aplicar.
	 */
	public void applyItemStats(Player player, StatsItem item) {

		IDBCPlayer dbcPlayer = plugin.getDbcManager().getDBC(player);
		if (dbcPlayer == null || item.getStatBonuses() == null) {
			return;
		}

		for (StatBonus bonus : item.getStatBonuses()) {
			String statName = bonus.getStatType().name().toLowerCase();
			String bonusID = bonus.getBonusID();

			switch (bonus.getModifierType()) {
				// Operaciones que acepta DBC: +, -, %, *, /
				case ADD:
					if (bonus.getValue() < 0) {
						dbcPlayer.addBonusAttribute(statName, bonusID, "-", bonus.getValue());
					} else {
						dbcPlayer.addBonusAttribute(statName, bonusID, "+", bonus.getValue());
					}
					break;
				case PERCENT:
					double multiplier = 1.0 + (bonus.getValue() / 100.0);
					dbcPlayer.addBonusAttribute(statName, bonusID, "*", multiplier);
					break;
				case MULTIPLY:
					dbcPlayer.addBonusAttribute(statName, bonusID, "*", bonus.getValue());
					break;
				case DIVIDE:
					dbcPlayer.addBonusAttribute(statName, bonusID, "/", bonus.getValue());
					break;
			}
		}
	}

	/**
	 * Remueve los bonus de un ítem de un jugador.
	 * @param player El jugador.
	 * @param item El StatsItem a remover.
	 */
	public void removeItemStats(Player player, StatsItem item) {
		IDBCPlayer dbcPlayer = plugin.getDbcManager().getDBC(player);
		if (dbcPlayer == null || item.getStatBonuses() == null) {
			return;
		}

		for (StatBonus bonus : item.getStatBonuses()) {
			String statName = bonus.getStatType().name().toLowerCase();
			String bonusID = bonus.getBonusID();

			dbcPlayer.removeBonusAttribute(statName, bonusID);
		}
	}

	public void setPlayerLoreEditing(Player player, StatsItem item, int line) {
		playerLoreEditing.put(player.getUniqueId(), new Object[]{item, line});
	}

	public Object[] getPlayerLoreEditing(Player player) {
		return playerLoreEditing.remove(player.getUniqueId());
	}

	public boolean isPlayerEditingLore(Player player) {
		return playerLoreEditing.containsKey(player.getUniqueId());
	}

	public void setFinalLoreEditing(Player player, StatsItem item, int line) {
		finalLoreEditing.put(player.getUniqueId(), new Object[]{item, line});
	}

	public Object[] getFinalLoreEditing(Player player) {
		return finalLoreEditing.remove(player.getUniqueId());
	}

	public boolean isFinalEditingLore(Player player) {
		return finalLoreEditing.containsKey(player.getUniqueId());
	}

	/**
	 * Convierte nuestro Enum de Stat al ID numérico que requiere la API.
	 * @param stat El tipo de Stat de nuestro Enum.
	 * @return El ID numérico (0-5), o -1 si es inválido.
	 */
	private int getStatId(Stat stat) {
		switch (stat) {
			case STR: return 0;
			case DEX: return 1;
			case CON: return 2;
			case WIL: return 3;
			case MND: return 4;
			case SPI: return 5;
			default: return -1;
		}
	}
}