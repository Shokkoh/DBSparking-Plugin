package com.shokkoh.dbsparking.database;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.items.*;
import com.shokkoh.dbsparking.utils.Text;
import com.shokkoh.dbsparking.utils.Logger;
import org.bukkit.Material;

import java.sql.*;
import java.util.*;

public class ItemDataManager {

	private final DBSparking plugin;
	private final Connection connection;

	public ItemDataManager(DBSparking plugin) {
		this.plugin = plugin;
		this.connection = plugin.getDatabaseManager().getConnection();
	}
	/**
	 * Guarda un nuevo StatsItem en la tabla 'dbs_items'.
	 * @param item El objeto StatsItem a guardar.
	 * @return El ID del nuevo ítem generado por la base de datos, o -1 si falla.
	 */
	public int createCustomItem(StatsItem item) {
		String sql = "INSERT INTO dbs_items (internal_name, item_type, display_name, material, item_data, rarity) VALUES (?, ?, ?, ?, ?, ?);";
		try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, item.getInternalName());
			pstmt.setString(2, item.getItemType().name());
			pstmt.setString(3, item.getDisplayName());
			pstmt.setString(4, item.getMaterial().name());
			pstmt.setInt(5, item.getItemData());
			pstmt.setString(6, item.getRarity().name());

			if (pstmt.executeUpdate() > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getInt(1);
					}
				}
			}
		} catch (SQLException e) {
			Logger.severe("Could not create custom item: " + item.getInternalName());
		}
		return -1;
	}

	/**
	 * Verifica si ya existe un ítem con un nombre interno específico.
	 * @param internalName El nombre a verificar.
	 * @return true si ya existe, false si no.
	 */
	public boolean itemExists(String internalName) {
		String sql = "SELECT id FROM dbs_items WHERE internal_name = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, internalName);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			Logger.severe("Could not check for existing item");
			return true;
		}
	}

	/**
	 * Añade o actualiza un bonus de stat para un ítem. Si ya existe un bonus para esa
	 * stat, lo actualiza. Si no, lo crea.
	 */
	public void addOrUpdateStatBonus(int itemId, StatBonus bonus) {
		removeStatBonusFromItem(itemId, bonus.getStatType());
		addStatBonusToItem(itemId, bonus);
	}

	/**
	 * Añade un nuevo bonus de estadística a un ítem existente.
	 * @param itemId El ID del ítem.
	 * @param bonus El objeto StatBonus a guardar.
	 * @return El ID del nuevo bonus, o -1 si falla.
	 */
	private int addStatBonusToItem(int itemId, StatBonus bonus) {
		String sql = "INSERT INTO dbs_item_stats (item_id, bonus_id, stat_type, modifier_type, value) VALUES (?, ?, ?, ?, ?);";
		try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setInt(1, itemId);
			pstmt.setString(2, bonus.getBonusID());
			pstmt.setString(3, bonus.getStatType().name());
			pstmt.setString(4, bonus.getModifierType().name());
			pstmt.setDouble(5, bonus.getValue());

			if (pstmt.executeUpdate() > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						return rs.getInt(1);
					}
				}
			}
		} catch (SQLException e) {
			Logger.severe("Could not add stat bonus to item " + itemId);
		}
		return -1;
	}

	/**
	 * Elimina todos los bonus de una estadística específica de un ítem.
	 * @param itemId El ID del ítem.
	 * @param stat El tipo de stat a eliminar (ej. STR).
	 * @return true si se eliminó algo, false si no.
	 */
	public boolean removeStatBonusFromItem(int itemId, Stat stat) {
		String sql = "DELETE FROM dbs_item_stats WHERE item_id = ? AND stat_type = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			pstmt.setString(2, stat.name());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			Logger.severe("Could not remove stat bonus from item " + itemId);
			return false;
		}
	}

	/**
	 * Obtiene un StatsItem completo desde la base de datos por su nombre interno.
	 * @param internalName El nombre único del ítem (ej. "PecheraVIP").
	 * @return El objeto StatsItem completo con sus stats y lore, o null si no se encuentra.
	 */
	public StatsItem getItemByName(String internalName) {
		String sql = "SELECT * FROM dbs_items WHERE internal_name = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, internalName);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					StatsItem item = new StatsItem();
					int itemId = rs.getInt("id");
					item.setId(itemId);
					item.setInternalName(rs.getString("internal_name"));
					item.setItemType(ItemType.valueOf(rs.getString("item_type")));
					item.setDisplayName(rs.getString("display_name"));
					item.setMaterial(Material.valueOf(rs.getString("material")));
					item.setItemData(rs.getInt("item_data"));

					item.setMinLevel(rs.getInt("min_level"));
					item.setMaxLevel(rs.getInt("max_level"));
					item.setRequiredRank(rs.getString("required_rank"));
					item.setRarity(Rarity.valueOf(rs.getString("rarity")));
					item.setFinalLore(getFinalLoreLinesForItem(itemId));

					// Cargar las stats y el lore asociados
					item.setStatBonuses(getStatBonusesForItem(itemId));
					item.setCustomLore(getLoreLinesForItem(itemId));

					return item;
				}
			}
		} catch (SQLException | IllegalArgumentException e) {
			Logger.info("Could not retrieve item by name: " + internalName);
		}
		return null;
	}

	/**
	 * Obtiene las stats de un ítem específico.
	 * @param itemId El ID del ítem en la base de datos.
	 * @return Una lista de objetos StatBonus.
	 */
	public List<StatBonus> getStatBonusesForItem(int itemId) {
		List<StatBonus> bonuses = new ArrayList<>();
		String sql = "SELECT * FROM dbs_item_stats WHERE item_id = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					StatBonus bonus = new StatBonus();
					bonus.setId(rs.getInt("id"));
					bonus.setItemId(itemId);
					bonus.setBonusID(rs.getString("bonus_id"));
					bonus.setStatType(Stat.valueOf(rs.getString("stat_type")));
					bonus.setModifierType(ModifierType.valueOf(rs.getString("modifier_type")));
					bonus.setValue(rs.getDouble("value"));
					bonuses.add(bonus);
				}
			}
		} catch (SQLException | IllegalArgumentException e) {
			Logger.severe("Could not retrieve stats for item ID: " + itemId);
		}
		return bonuses;
	}

	/**
	 * Obtiene las líneas de lore de un ítem específico.
	 * @param itemId El ID del ítem en la base de datos.
	 * @return Una lista de Strings con el lore.
	 */
	public List<String> getLoreLinesForItem(int itemId) {
		List<String> lore = new ArrayList<>();
		String sql = "SELECT line_text FROM dbs_item_lore WHERE item_id = ? ORDER BY line_number ASC;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					lore.add(rs.getString("line_text"));
				}
			}
		} catch (SQLException e) {
			Logger.severe("Could not retrieve lore for item ID: " + itemId);
		}
		return lore;
	}

	public void updateLoreLine(int itemId, int lineNumber, String text) {
		removeLoreLine(itemId, lineNumber);
		addLoreLineToItem(itemId, lineNumber, text);
	}

	public void addLoreLineToItem(int itemId, int lineNumber, String text) {
		String sql = "INSERT INTO dbs_item_lore (item_id, line_number, line_text) VALUES (?, ?, ?);";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			pstmt.setInt(2, lineNumber);
			pstmt.setString(3, text);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			Logger.severe("Could not add lore line to item " + itemId);
		}
	}

	public void removeFinalLoreLine(int itemId, int lineOrder) {
		String sql = "DELETE FROM dbs_item_final_lore WHERE item_id = ? AND line_order = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			pstmt.setInt(2, lineOrder);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			Logger.severe("Could not remove final lore line from item " + itemId);
		}
	}

	public void updateFinalLoreLine(int itemId, int lineOrder, String text) {
		removeFinalLoreLine(itemId, lineOrder);
		addFinalLoreToItem(itemId, lineOrder, text);
	}

	/**
	 * Añade una nueva línea de lore al final de la descripción de un ítem.
	 * @param itemId El ID del ítem.
	 * @param lineNumber El número de línea (0-indexed).
	 * @param text La línea de texto a añadir.
	 */
	public void addFinalLoreToItem(int itemId, int lineNumber, String text) {
		String sql = "INSERT INTO dbs_item_final_lore (item_id, line_number, line_text) VALUES (?, ?, ?);";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			pstmt.setInt(2, lineNumber);
			pstmt.setString(3, text);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			Logger.severe("Could not add lore line to item " + itemId);
		}
	}

	/**
	 * Obtiene las líneas de lore final de un ítem específico, ordenadas.
	 * @param itemId El ID del ítem en la base de datos.
	 * @return Una lista de Strings con el lore final.
	 */
	public List<String> getFinalLoreLinesForItem(int itemId) {
		List<String> lore = new ArrayList<>();
		String sql = "SELECT line_text FROM dbs_item_final_lore WHERE item_id = ? ORDER BY line_order ASC;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					lore.add(rs.getString("line_text"));
				}
			}
		} catch (SQLException e) {
			Logger.severe("Could not retrieve final lore for item ID: " + itemId);
		}
		return lore;
	}

	public void removeLoreLine(int itemId, int lineNumber) {
		String sql = "DELETE FROM dbs_item_lore WHERE item_id = ? AND line_number = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, itemId);
			pstmt.setInt(2, lineNumber);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			Logger.severe("Could not remove lore line from item " + itemId);
		}
	}

	public List<StatsItem> getAllItems() {
		Map<Integer, StatsItem> itemsById = new HashMap<>();
		String itemsSql = "SELECT * FROM dbs_items;";
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(itemsSql)) {
			while (rs.next()) {
				StatsItem item = new StatsItem();
				int itemId = rs.getInt("id");
				item.setId(itemId);
				item.setInternalName(rs.getString("internal_name"));
				item.setItemType(ItemType.valueOf(rs.getString("item_type")));
				item.setDisplayName(rs.getString("display_name"));
				item.setMaterial(Material.valueOf(rs.getString("material")));
				item.setItemData(rs.getInt("item_data"));

				item.setStatBonuses(new ArrayList<StatBonus>());
				item.setCustomLore(new ArrayList<String>());

				itemsById.put(itemId, item);
			}
		} catch (SQLException | IllegalArgumentException e) {
			Logger.severe("Could not retrieve all custom items");
			return new ArrayList<>();
		}

		String statsSql = "SELECT * FROM dbs_item_stats;";
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(statsSql)) {
			while (rs.next()) {
				int itemId = rs.getInt("item_id");
				StatsItem parentItem = itemsById.get(itemId);
				if (parentItem != null) {
					StatBonus bonus = new StatBonus();
					bonus.setId(rs.getInt("id"));
					bonus.setItemId(itemId);
					bonus.setStatType(Stat.valueOf(rs.getString("stat_type")));
					bonus.setModifierType(ModifierType.valueOf(rs.getString("modifier_type")));
					bonus.setValue(rs.getDouble("value"));
					parentItem.getStatBonuses().add(bonus);
				}
			}
		} catch (SQLException | IllegalArgumentException e) {
			Logger.severe("Could not retrieve all stat bonuses");
		}

		String loreSql = "SELECT * FROM dbs_item_lore ORDER BY item_id, line_number ASC;";
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(loreSql)) {
			while (rs.next()) {
				int itemId = rs.getInt("item_id");
				StatsItem parentItem = itemsById.get(itemId);
				if (parentItem != null) {
					parentItem.getCustomLore().add(rs.getString("line_text"));
				}
			}
		} catch (SQLException e) {
			Logger.severe("Could not retrieve all lore lines");
		}

		return new ArrayList<>(itemsById.values());
	}

	public void updateItem(String oldInternalName, StatsItem updatedItem) {
		String sql = "UPDATE dbs_items SET internal_name = ?, display_name = ? WHERE internal_name = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, updatedItem.getInternalName());
			pstmt.setString(2, updatedItem.getDisplayName());
			pstmt.setString(3, oldInternalName);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			Logger.severe("Could not update custom item");
		}
	}

	public List<String> getStatLoreFor(StatsItem item) {
		List<String> statLore = new ArrayList<>();
		if (item.getStatBonuses() == null || item.getStatBonuses().isEmpty()) {
			return statLore;
		}

		statLore.add(plugin.getLanguage().getRawMessage("item_lore_header"));
		for (StatBonus bonus : item.getStatBonuses()) {
			String line = "";
			String statName = bonus.getStatType().name();
			String prefix = plugin.getLanguage().getRawMessage("item_lore_prefixes." + statName.toLowerCase());

			switch (bonus.getModifierType()) {
				case ADD:
					line = String.format(prefix + " %s%.0f %s", (bonus.getValue() >= 0 ? "+" : ""), bonus.getValue(), statName);
					break;
				case MULTIPLY:
					line = String.format(prefix + " x%.2f %s", bonus.getValue(), statName);
					break;
				case PERCENT:
					line = String.format(prefix + " %s%.0f%% %s", (bonus.getValue() >= 0 ? "+" : ""), bonus.getValue(), statName);
					break;
				case DIVIDE:
					line = String.format(prefix + " /%.2f %s", bonus.getValue(), statName);
					break;
			}
			statLore.add(Text.color(line));
		}
		return statLore;
	}

	/**
	 * Elimina un StatsItem de la base de datos por su nombre interno.
	 * @param internalName El nombre único del ítem.
	 * @return true si se eliminó, false si no se encontró o hubo un error.
	 */
	public boolean deleteItemByName(String internalName) {
		String sql = "DELETE FROM dbs_items WHERE internal_name = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, internalName);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			Logger.severe("Could not delete custom item: " + internalName);
			return false;
		}
	}

	/**
	 * Actualiza las condiciones de nivel y rango de un ítem.
	 * @param itemId El ID del ítem a actualizar.
	 * @param minLevel El nivel mínimo.
	 * @param maxLevel El nivel máximo.
	 * @param rank El rango requerido.
	 */
	public void updateItemConditions(int itemId, int minLevel, int maxLevel, String rank) {
		String sql = "UPDATE dbs_items SET min_level = ?, max_level = ?, required_rank = ? WHERE id = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, minLevel);
			pstmt.setInt(2, maxLevel);
			pstmt.setString(3, rank);
			pstmt.setInt(4, itemId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			Logger.severe("Could not update item conditions for item " + itemId);
		}
	}

	/**
	 * OBtiene un mapa de las almas equipadas por un jugador desde la base de datos.
	 * @param playerUUID El UUID del jugador.
	 * @return Un mapa con el slot (1 o 2) como clave y el nombre interno del ítem como valor.
	 */
	public Map<Integer, String> getEquippedSoulsForPlayer(UUID playerUUID) {
		Map<Integer, String> equippedSouls = new HashMap<>();
		String sql = "SELECT slot, item_internal_name FROM dbs_equipped_souls WHERE uuid = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, playerUUID.toString());
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					int slot = rs.getInt("slot");
					String itemName = rs.getString("item_internal_name");
					equippedSouls.put(slot, itemName);
				}
			}
		} catch (SQLException e) {
			Logger.severe("Could not retrieve equipped souls for player " + playerUUID);
		}
		return equippedSouls;
	}

	/**
	 * Guarda o actualiza un alma equipada para un jugador en la base de datos.
	 * @param playerUUID El UUID del jugador.
	 * @param slot El slot(1 o 2).
	 * @param itemInternalName El nombre interno delítem equipado.
	 */
	public void equipSoul(UUID playerUUID, int slot, String itemInternalName) {
		String sql = "INSERT OR REPLACE INTO dbs_equipped_souls (uuid, slot, item_internal_name) VALUES (?, ?, ?);";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, playerUUID.toString());
			pstmt.setInt(2, slot);
			pstmt.setString(3, itemInternalName);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			Logger.severe("Could not equip soul for player " + playerUUID);
		}
	}

	/**
	 * Elimina una alma equipada de un jugador en un slot específico en la base de datos.
	 * @param playerUUID El UUID del jugador.
	 * @param slot El slot (1 o 2) del que se desequipa el alma.
	 */
	public void unequipSoul(UUID playerUUID, int slot) {
		String sql = "DELETE FROM dbs_equipped_souls WHERE uuid = ? AND slot = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, playerUUID.toString());
			pstmt.setInt(2, slot);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			Logger.severe("Could not unequip soul for player " + playerUUID);
		}
	}
}