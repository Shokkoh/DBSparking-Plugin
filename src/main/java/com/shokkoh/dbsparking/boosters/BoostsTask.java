package com.shokkoh.dbsparking.boosters;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.entities.DBSPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BoostsTask extends BukkitRunnable {

	private final DBSparking plugin = DBSparking.getInstance();
	private int tickCounter = 0;

	@Override
	public void run() {
		long currentTime = System.currentTimeMillis();

		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			DBSPlayer dbsPlayer = plugin.getPlayerManager().getDBSPlayer(player);
			if (dbsPlayer == null || dbsPlayer.getActiveBoosts().isEmpty()) {
				continue;
			}

			Iterator<ActiveBoost> personalIterator = dbsPlayer.getActiveBoosts().iterator();
			while (personalIterator.hasNext()) {
				ActiveBoost boost = personalIterator.next();
				if (boost.getDuration() != -1 && boost.getExpirationTimestamp() != -1 && currentTime >= boost.getExpirationTimestamp()) {
					handleExpiredBoost(boost);
					personalIterator.remove();
				}
			}
		}

		tickCounter++;
		if (tickCounter >= 15) {
			tickCounter = 0;
			List<ActiveBoost> nonPersonalCache = plugin.getBoostManager().getNonPersonalBoostsCache();
			Iterator<ActiveBoost> nonPersonalIterator = nonPersonalCache.iterator();

			while (nonPersonalIterator.hasNext()) {
				ActiveBoost boost = nonPersonalIterator.next();
				if (boost.getDuration() == -1 || boost.getExpirationTimestamp() == -1) continue;

				if (currentTime >= boost.getExpirationTimestamp()) {
					handleExpiredBoost(boost);
					nonPersonalIterator.remove();
				} else {
					long remainingMillis = boost.getExpirationTimestamp() - currentTime;
					long remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis);
					plugin.getBoostDataManager().updateBoostDuration(boost.getId(), remainingSeconds);
				}
			}
		}
	}

	private void handleExpiredBoost(ActiveBoost boost) {
		plugin.getBoostDataManager().deleteBoostById(boost.getId());

		Sound expireSound;
		try {
			expireSound = Sound.valueOf(plugin.getConfig().getString("sounds.boost_expire", "FIZZ"));
		} catch (IllegalArgumentException e) {
			expireSound = Sound.FIZZ;
		}

		switch (boost.getBoostType()) {
			case GLOBAL:
				String globalMsg = plugin.getLanguage().getMessage("boost_global_expired")
						.replace("%name%", boost.getName())
						.replace("%author%", boost.getAuthor());
				Bukkit.broadcastMessage(globalMsg);
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					p.playSound(p.getLocation(), expireSound, 1.0f, 1.0f);
				}
				break;
			case PERSONAL:
				Player target = Bukkit.getPlayer(boost.getTarget());
				if (target != null && target.isOnline()) {
					target.sendMessage(plugin.getLanguage().getMessage("boost_personal_expired").replace("%name%", boost.getName()));
					target.playSound(target.getLocation(), expireSound, 1.0f, 1.0f);
				}
				break;
			case RANK:
				String rankMsg = plugin.getLanguage().getMessage("boost_rank_expired")
						.replace("%name%", boost.getName())
						.replace("%rank%", boost.getTarget());
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					if (DBSparking.perms != null && DBSparking.perms.playerInGroup(p, boost.getTarget())) {
						p.sendMessage(rankMsg);
						p.playSound(p.getLocation(), expireSound, 1.0f, 1.0f);
					}
				}
				break;
		}
	}
}
