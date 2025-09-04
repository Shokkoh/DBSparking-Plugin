package com.shokkoh.dbsparking.database;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.boosters.ActiveBoost;
import com.shokkoh.dbsparking.boosters.BoostType;
import com.shokkoh.dbsparking.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BoostDataManager {
	private final DBSparking plugin;
	private final Connection connection;

	public BoostDataManager(DBSparking plugin) {
		this.plugin = plugin;
		this.connection = plugin.getDatabaseManager().getConnection();
	}

	/**
	 * Guarda un nuevo booster activo en la base de datos.
	 * @param boost El objeto ActiveBoost con toda la información.
	 * @return El ID del nuevo booster, o -1 si hubo un error.
	 */
	public int addBoost(ActiveBoost boost) {
		String sql = "INSERT INTO dbs_active_boosts (name, boost_type, target, target_name, amount, duration, last_updated, author) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
		try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, boost.getName());
			pstmt.setString(2, boost.getBoostType().name());
			pstmt.setString(3, boost.getTarget());
			pstmt.setString(4, boost.getTargetName());
			pstmt.setDouble(5, boost.getAmount());
			pstmt.setLong(6, boost.getDuration());
			pstmt.setTimestamp(7, new Timestamp(boost.getLastUpdated().getTime()));
			pstmt.setString(8, boost.getAuthor());

			if (pstmt.executeUpdate() > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) { return generatedKeys.getInt(1); }
				}
			}
		} catch (SQLException e) {
			Logger.severe("Could not save boost to the database");
		}
		return -1;
	}

	/**
	 * Verifica si un jugador específico ya tiene un booster personal con ese nombre.
	 * @param playerUUID El UUID del jugador.
	 * @param boostName El nombre del booster a verificar.
	 * @return true si el jugador ya tiene ese booster, false si no.
	 */
	public boolean personalBoostExists(UUID playerUUID, String boostName) {
		String sql = "SELECT id FROM dbs_active_boosts WHERE boost_type = 'PERSONAL' AND target = ? AND name = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, playerUUID.toString());
			pstmt.setString(2, boostName);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			Logger.severe("Could not check for existing personal boost");
			return true;
		}
	}

	private ActiveBoost buildBoostFromResultSet(ResultSet rs) throws SQLException {
		ActiveBoost boost = new ActiveBoost();
		boost.setId(rs.getInt("id"));
		boost.setName(rs.getString("name"));
		boost.setBoostType(BoostType.valueOf(rs.getString("boost_type")));
		boost.setTarget(rs.getString("target"));
		boost.setTargetName(rs.getString("target_name"));
		boost.setAmount(rs.getDouble("amount"));
		boost.setDuration(rs.getLong("duration"));
		boost.setLastUpdated(rs.getTimestamp("last_updated"));
		boost.setAuthor(rs.getString("author"));
		return boost;
	}

	/**
	 * Obtiene una lista de todos los boosters activos desde la base de datos.
	 * @return Una lista de objetos ActiveBoost.
	 */
	public List<ActiveBoost> getActiveBoosts() {
		List<ActiveBoost> boosts = new ArrayList<>();
		String sql = "SELECT * FROM dbs_active_boosts;";
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				boosts.add(buildBoostFromResultSet(rs));
			}
		} catch (SQLException e) {
			Logger.severe("Could not retrieve active boosts");
		}
		return boosts;
	}

	public List<ActiveBoost> getActivePersonalBoostsFor(UUID playerUUID) {
		List<ActiveBoost> boosts = new ArrayList<>();
		String sql = "SELECT * FROM dbs_active_boosts WHERE boost_type = 'PERSONAL' AND target = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, playerUUID.toString());
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					boosts.add(buildBoostFromResultSet(rs));
				}
			}
		} catch (SQLException e) {
			Logger.severe("Could not get personal boosts for player " + playerUUID);
		}
		return boosts;
	}

	/**
	 * Elimina un booster de la base de datos por su nombre único.
	 * @param name El nombre del booster a eliminar.
	 * @return true si se eliminó al menos una fila, false si no se encontró o hubo un error.
	 */
	public boolean deleteBoost(String name) {
		String sql = "DELETE FROM dbs_active_boosts WHERE name = ?;";

		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, name);
			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			Logger.severe("Could not delete boost from the database");
			return false;
		}
	}

	public boolean deleteBoostById(int id) {
		String sql = "DELETE FROM dbs_active_boosts WHERE id = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			Logger.severe("Could not delete boost by ID");
			return false;
		}
	}

	/**
	 * Verifica si ya existe un booster con un nombre específico.
	 * @param name El nombre a verificar.
	 * @return true si el nombre ya está en uso, false si no.
	 */
	public boolean boostExists(String name) {
		String sql = "SELECT id FROM dbs_active_boosts WHERE name = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, name);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			Logger.severe("Could not check for existing boost");
			return true;
		}
	}

	public void updateBoostDuration(int boostId, long newDuration) {
		String sql = "UPDATE dbs_active_boosts SET duration = ? WHERE id = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setLong(1, newDuration);
			pstmt.setInt(2, boostId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			Logger.severe("Could not update boost duration");
		}
	}

	/**
	 * Elimina un booster personal específico de un jugador por el nombre del booster.
	 * @param playerUUID El UUID del jugador.
	 * @param boostName El nombre del booster a eliminar.
	 * @return true si se eliminó, false si no se encontró.
	 */
	public boolean deletePersonalBoost(UUID playerUUID, String boostName) {
		String sql = "DELETE FROM dbs_active_boosts WHERE boost_type = 'PERSONAL' AND target = ? AND name = ?;";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, playerUUID.toString());
			pstmt.setString(2, boostName);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			Logger.severe("Could not delete personal boost");
			return false;
		}
	}

	public boolean deleteAllBoostsForPlayer(UUID playerUUID) {
		String sql = "DELETE FROM dbs_active_boosts WHERE target = ? AND boost_type = 'PERSONAL';";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, playerUUID.toString());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			Logger.severe("Could not delete boosts for player " + playerUUID);
			return false;
		}
	}
}