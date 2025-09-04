package com.shokkoh.dbsparking.managers;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.boosters.ActiveBoost;
import com.shokkoh.dbsparking.boosters.BoostType;
import com.shokkoh.dbsparking.entities.DBSPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BoostManager {
	private final DBSparking plugin;
	private final List<ActiveBoost> nonPersonalBoostsCache = new ArrayList<>();

	public BoostManager(DBSparking plugin) {
		this.plugin = plugin;
	}

	/**
	 * Calcula el multiplicador de TP final para un jugador.
	 * @param player El jugador.
	 * @return El multiplicador final (ej. 1.0 para sin boost, 1.5 para +50%).
	 */
	public double getFinalBoost(Player player) {
		double totalBoostAmount = 0.0;
		long currentTime = System.currentTimeMillis();

		DBSPlayer dbsPlayer = plugin.getPlayerManager().getDBSPlayer(player);
		if (dbsPlayer != null) {
			for (ActiveBoost boost : dbsPlayer.getActiveBoosts()) {
				if (boost.getDuration() == -1 || (boost.getExpirationTimestamp() != -1 && boost.getExpirationTimestamp() > currentTime)) {
					totalBoostAmount += boost.getAmount();
				}
			}
		}

		for (ActiveBoost boost : this.nonPersonalBoostsCache) {
			boolean applies = false;
			switch (boost.getBoostType()) {
				case GLOBAL:
					applies = true;
					break;
				case RANK:
					Player freshPlayer = Bukkit.getServer().getPlayer(player.getName());
					if (DBSparking.perms != null && freshPlayer != null && DBSparking.perms.playerInGroup(freshPlayer, boost.getTarget())) {
						applies = true;
					}
					break;
				default:
					break;
			}

			if (applies) {
				if (boost.getDuration() == -1 || (boost.getExpirationTimestamp() != -1 && boost.getExpirationTimestamp() > currentTime)) {
					totalBoostAmount += boost.getAmount();
				}
			}
		}

		return 1.0 + (totalBoostAmount / 100.0);
	}

	/**
	 * Obtiene todos los boosters globales activos desde la caché.
	 * @return Una lista de boosters globales.
	 */
	public List<ActiveBoost> getActiveGlobalBoosts() {
		List<ActiveBoost> globals = new ArrayList<>();
		for (ActiveBoost boost : this.nonPersonalBoostsCache) {
			if (boost.getBoostType() == BoostType.GLOBAL) {
				globals.add(boost);
			}
		}
		return globals;
	}

	/**
	 * Obtiene los boosters de rango que aplican a un jugador desde la caché.
	 * @param player El jugador.
	 * @return Una lista de boosters de rango.
	 */
	public List<ActiveBoost> getActiveRankBoosts(Player player) {
		List<ActiveBoost> ranks = new ArrayList<>();
		if (DBSparking.perms == null) return ranks;

		for (ActiveBoost boost : this.nonPersonalBoostsCache) {
			if (boost.getBoostType() == BoostType.RANK && DBSparking.perms.playerInGroup(player, boost.getTarget())) {
				ranks.add(boost);
			}
		}
		return ranks;
	}

	/**
	 * Carga/Recarga boosters Globales y de Rango desde la BBDD a la caché.
	 */
	public void loadBoostsFromDatabase() {
		this.nonPersonalBoostsCache.clear();
		List<ActiveBoost> allBoosts = plugin.getBoostDataManager().getActiveBoosts();
		long serverStartTime = System.currentTimeMillis();

		for (ActiveBoost boost : allBoosts) {
			if (boost.getBoostType() != BoostType.PERSONAL && boost.getDuration() != -1) {
				long lastUpdated = boost.getLastUpdated().getTime();
				long offlineMillis = serverStartTime - lastUpdated;
				long offlineSeconds = TimeUnit.MILLISECONDS.toSeconds(offlineMillis);

				long newDuration = boost.getDuration() - offlineSeconds;

				if (newDuration <= 0) {
					plugin.getBoostDataManager().deleteBoostById(boost.getId());
					continue;
				}

				boost.setDuration(newDuration);
				boost.setExpirationTimestamp(serverStartTime + TimeUnit.SECONDS.toMillis(newDuration));
			}
			if (boost.getBoostType() != BoostType.PERSONAL) {
				this.nonPersonalBoostsCache.add(boost);
			}
		}
	}

	/**
	 * Devuelve la caché de boosters globales y de rango.
	 * @return Una lista de boosters activos.
	 */
	public List<ActiveBoost> getNonPersonalBoostsCache() {
		return nonPersonalBoostsCache;
	}

	/**
	 * Elimina un booster de la caché en memoria por su ID.
	 * @param boostId El ID del booster a eliminar.
	 */
	public void removeBoostFromCache(int boostId) {
		Iterator<ActiveBoost> iterator = this.nonPersonalBoostsCache.iterator();
		while (iterator.hasNext()) {
			ActiveBoost boost = iterator.next();
			if (boost.getId() == boostId) {
				iterator.remove();
				break;
			}
		}
	}

	/**
	 * Busca un booster en la caché de globales/rango por su nombre.
	 * @param name El nombre del booster.
	 * @return El objeto ActiveBoost si se encuentra, o null.
	 */
	public ActiveBoost getBoostFromCache(String name) {
		for (ActiveBoost boost : this.nonPersonalBoostsCache) {
			if (boost.getName().equalsIgnoreCase(name)) {
				return boost;
			}
		}
		return null;
	}
}