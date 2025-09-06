package com.shokkoh.dbsparking.managers;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.boosters.ActiveBoost;
import com.shokkoh.dbsparking.entities.DBSPlayer;
import com.shokkoh.dbsparking.utils.Logger;
import com.shokkoh.dbsparking.utils.NumberFormats;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Gestiona el ciclo de vida de los objetos DBSPlayer.
 */
public class PlayerManager {
	private final Map<UUID, DBSPlayer> players = new HashMap<>();
	private final DBSparking plugin;

	public  PlayerManager() {
		this.plugin = DBSparking.getInstance();
	}

	/**
	 * Carga los datos de un jugador cuando se une al servidor.
	 * @param player El jugador que se ha conectado.
	 */
	public void handlePlayerJoin(Player player) {
		DBSPlayer dbsPlayer = new DBSPlayer(player);
		List<ActiveBoost> personalBoosts = plugin.getBoostDataManager().getActivePersonalBoostsFor(player.getUniqueId());
		for (ActiveBoost boost : personalBoosts) {
			if (boost.getDuration() != -1) {
				boost.setExpirationTimestamp(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(boost.getDuration()));
			}
			String timeStr = (boost.getDuration() == -1) ? "Permanent" : NumberFormats.formatDuration(boost.getDuration());
			player.sendMessage(DBSparking.getInstance().getLanguage().getMessage("boost_personal_active_on_login")
					.replace("%name%", boost.getName())
					.replace("%time%", timeStr));
		}
		dbsPlayer.setActiveBoosts(personalBoosts);

		this.loadUnlockedSlots(dbsPlayer);
		dbsPlayer.loadEquippedSouls();

		players.put(player.getUniqueId(), dbsPlayer);
		updatePlayerDatabase(dbsPlayer);
	}

	/**
	 * Elimina los datos de un jugador cuando abandona el servidor.
	 * @param player El jugador que se ha desconectado.
	 */
	public void handlePlayerQuit(Player player) {
		DBSPlayer dbsPlayer = players.get(player.getUniqueId());
		if (dbsPlayer == null) return;

		for (ActiveBoost boost : dbsPlayer.getActiveBoosts()) {
			if (boost.getDuration() != -1 && boost.getExpirationTimestamp() != -1) {
				long remainingMillis = boost.getExpirationTimestamp() - System.currentTimeMillis();

				long remainingSeconds = Math.max(0, TimeUnit.MILLISECONDS.toSeconds(remainingMillis));

				DBSparking.getInstance().getBoostDataManager().updateBoostDuration(boost.getId(), remainingSeconds);
			}
		}
		players.remove(player.getUniqueId());
		updatePlayerDatabase(dbsPlayer);
	}

	/**
	 * Obtiene el objeto DBSPlayer para un jugador de Bukkit.
	 * @param player El jugador de Bukkit.
	 * @return El DBSPlayer correspondiente, o null si no se encuentra.
	 */
	public DBSPlayer getDBSPlayer(Player player) {
		return players.get(player.getUniqueId());
	}

	/**
	 * Obtiene el objeto DBSPlayer por su nombre de jugador.
	 * @param name El nombre del jugador.
	 * @return El DBSPlayer correspondiente, o null si no se encuentra.
	 */
	public DBSPlayer getDBSPlayerName(String name) {
		for (DBSPlayer onlineDbsPlayer : players.values()) {
			if (onlineDbsPlayer.getPlayer().getName().equalsIgnoreCase(name)) {
				return onlineDbsPlayer;
			}
		}

		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
		if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
			return null;
		}

		String sql = "SELECT level, race_id FROM dbs_players WHERE uuid = ?;";
		try (PreparedStatement pstmt = plugin.getDatabaseManager().getConnection().prepareStatement(sql)) {
			pstmt.setString(1, offlinePlayer.getUniqueId().toString());
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				int level = rs.getInt("level");
				int raceId = rs.getInt("race_id");

				return new DBSPlayer(offlinePlayer, level, raceId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Player getPlayerByName(String name) {
		for (DBSPlayer dbsPlayer : players.values()) {
			if (dbsPlayer.getPlayer().getName().equalsIgnoreCase(name)) {
				return dbsPlayer.getPlayer();
			}
		}
		return null;
	}

	public void loadUnlockedSlots(DBSPlayer dbsPlayer) {
		List<Integer> unlocked = new ArrayList<>();

		unlocked.add(1);
		if (dbsPlayer.canEquipSoul(3)) {
			unlocked.add(3);
		}

		String sql = "SELECT unlocked_soul_slots FROM dbs_players WHERE uuid = ?;";
		try (PreparedStatement pstmt = plugin.getDatabaseManager().getConnection().prepareStatement(sql)) {
			pstmt.setString(1, dbsPlayer.getUuid().toString());
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				String slotsStr = rs.getString("unlocked_soul_slots");
				if (slotsStr != null && !slotsStr.isEmpty()) {
					for (String s : slotsStr.split(",")) {
						if (!s.isEmpty()) {
							Integer slotNum = Integer.parseInt(s);
							if (!unlocked.contains(slotNum)) {
								unlocked.add(slotNum);
							}
						}
					}
				}
			}
		} catch (SQLException | NumberFormatException e) {
			Logger.severe("Error loading unlocked soul slots for player " + dbsPlayer.getPlayer().getName());
		}

		dbsPlayer.setUnlockedSoulSlots(unlocked);
	}

	public void unlockSlot(DBSPlayer dbsPlayer, int slot) {
		if (dbsPlayer.getUnlockedSoulSlots().contains(slot)) {
			return;
		}

		dbsPlayer.getUnlockedSoulSlots().add(slot);

		List<String> purchasableSlotsToSave = new ArrayList<>();
		for (Integer unlockedSlot : dbsPlayer.getUnlockedSoulSlots()) {
			if (unlockedSlot == 2 || unlockedSlot == 4) {
				purchasableSlotsToSave.add(String.valueOf(unlockedSlot));
			}
		}

		String slotsDbString = String.join(",", purchasableSlotsToSave);

		String updateSql = "UPDATE dbs_players SET unlocked_soul_slots = ? WHERE uuid = ?;";
		try (PreparedStatement pstmt = plugin.getDatabaseManager().getConnection().prepareStatement(updateSql)) {
			pstmt.setString(1, slotsDbString);
			pstmt.setString(2, dbsPlayer.getUuid().toString());

			if (pstmt.executeUpdate() == 0) {
				String insertSql = "INSERT INTO dbs_players (uuid, username, unlocked_soul_slots) VALUES (?, ?, ?);";
				try (PreparedStatement insertPstmt = plugin.getDatabaseManager().getConnection().prepareStatement(insertSql)) {
					insertPstmt.setString(1, dbsPlayer.getUuid().toString());
					insertPstmt.setString(2, dbsPlayer.getPlayer().getName());
					insertPstmt.setString(3, slotsDbString);
					insertPstmt.executeUpdate();
				}
			}
		} catch (SQLException e) {
			Logger.severe("Error saving unlocked soul slots for player " + dbsPlayer.getPlayer().getName());
			e.printStackTrace();
		}
	}

	/**
	 * @param dbsPlayer El jugador cuyos datos se van a guardar.
	 */
	private void updatePlayerDatabase(DBSPlayer dbsPlayer) {
		String uuid = dbsPlayer.getUuid().toString();
		String username = dbsPlayer.getPlayer().getName();
		int level = dbsPlayer.getLevel();
		int raceId = dbsPlayer.getRaceId();

		String sql = "INSERT OR REPLACE INTO dbs_players (uuid, username, level, race_id) VALUES (?, ?, ?, ?);";

		try (PreparedStatement pstmt = plugin.getDatabaseManager().getConnection().prepareStatement(sql)) {
			pstmt.setString(1, uuid);
			pstmt.setString(2, username);
			pstmt.setInt(3, level);
			pstmt.setInt(4, raceId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}