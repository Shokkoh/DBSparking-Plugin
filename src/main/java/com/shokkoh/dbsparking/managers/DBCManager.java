package com.shokkoh.dbsparking.managers;

import noppes.npcs.api.entity.IDBCPlayer;
import noppes.npcs.scripted.NpcAPI;
import org.bukkit.entity.Player;

public class DBCManager {

	/**
	 * Obtiene de forma segura la instancia IDBCPlayer de un jugador.
	 * @param p El jugador de Bukkit.
	 * @return La instancia de IDBCPlayer, o null si hay un error.
	 */
	public IDBCPlayer getDBC(Player p) {
		try {
			if (p == null || !p.isOnline()) return null;
			return NpcAPI.Instance().getPlayer(p.getName()).getDBCPlayer();
		} catch (Throwable t) {
			return null;
		}
	}

	public int getTP(Player p) {
		IDBCPlayer dbc = getDBC(p);
		return (dbc != null) ? dbc.getTP() : 0;
	}

	public void addTP(Player p, int amount) {
		IDBCPlayer dbc = getDBC(p);
		int currentTP = (dbc != null) ? dbc.getTP() : 0;
		int maxDbcTps = 2000000000;
		if (dbc != null) {
			if ((currentTP + amount) >= maxDbcTps) {
				amount = maxDbcTps - currentTP;
			}
			dbc.setTP(currentTP + amount);
		}
	}

	public void setTP(Player p, int amount) {
		IDBCPlayer dbc = getDBC(p);
		if (dbc != null) dbc.setTP(amount);
	}

	public int getStat(Player p, String statCode) {
		IDBCPlayer dbc = getDBC(p);
		return (dbc != null) ? dbc.getStat(statCode) : 0;
	}

	public int getRace(Player p) {
		IDBCPlayer dbc = getDBC(p);
		return (dbc != null) ? dbc.getRace() : -1;
	}

	public int getForm(Player p) {
		IDBCPlayer dbc = getDBC(p);
		return (dbc != null) ? dbc.getForm() : -1;
	}

	public int getDBCClass(Player p) {
		IDBCPlayer dbc = getDBC(p);
		return (dbc != null) ? dbc.getDBCClass() : -1;
	}
}