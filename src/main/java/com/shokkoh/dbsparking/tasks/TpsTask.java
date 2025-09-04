package com.shokkoh.dbsparking.tasks;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.entities.DBSPlayer;
import com.shokkoh.dbsparking.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TpsTask extends BukkitRunnable {

	private final DBSparking plugin;
	private final PlayerManager playerManager;
	private final long comboDelayMillis;

	public TpsTask(DBSparking plugin) {
		this.plugin = plugin;
		this.playerManager = plugin.getPlayerManager();
		this.comboDelayMillis = plugin.getConfig().getLong("tps_combo_system.delay_seconds", 3) * 1000;
	}

	@Override
	public void run() {
		long currentTime = System.currentTimeMillis();

		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			DBSPlayer dbsPlayer = playerManager.getDBSPlayer(player);
			if (dbsPlayer == null || dbsPlayer.getTpsToGive() <= 0) {
				continue;
			}

			if (currentTime - dbsPlayer.getComboTimestamp() >= comboDelayMillis) {
				plugin.getDbcManager().addTP(player, dbsPlayer.getTpsToGive());

				dbsPlayer.addTps(0);
				dbsPlayer.setCombo(0);
				dbsPlayer.setComboTimestamp(0);
			}
		}
	}
}