package com.shokkoh.dbsparking.utils;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.boosters.ActiveBoost;
import com.shokkoh.dbsparking.entities.DBSPlayer;
import com.shokkoh.dbsparking.managers.BoostManager;
import com.shokkoh.dbsparking.managers.DBCManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DBSPlaceholders extends PlaceholderExpansion {

	private final DBSparking plugin;

	public DBSPlaceholders(DBSparking plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	@NotNull
	public String getAuthor() {
		return "Shokkoh";
	}

	@Override
	@NotNull
	public String getIdentifier() {
		return "dbsparking";
	}

	@Override
	@NotNull
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		if (player == null) return "";
		DBCManager dbcManager = plugin.getDbcManager();
		BoostManager boostManager = plugin.getBoostManager();

		switch (identifier.toLowerCase(Locale.ROOT)) {
			case "dbcrace":
				int raceId = dbcManager.getRace(player);
				return plugin.getLanguage().getRawMessage("placeholder.race_" + raceId);
			case "dbcform":
				return String.valueOf(dbcManager.getForm(player));
			case "dbctps":
				return NumberFormats.formatInt(dbcManager.getTP(player));
			case "dbcclass":
				int classId = dbcManager.getDBCClass(player);
				return plugin.getLanguage().getRawMessage("placeholder.class_" + classId);
			case "dbcstr":
				return NumberFormats.formatInt(dbcManager.getStat(player, "str"));
			case "dbcdex":
				return NumberFormats.formatInt(dbcManager.getStat(player, "dex"));
			case "dbccon":
				return NumberFormats.formatInt(dbcManager.getStat(player, "con"));
			case "dbcwill":
				return NumberFormats.formatInt(dbcManager.getStat(player, "wil"));
			case "dbcmnd":
				return NumberFormats.formatInt(dbcManager.getStat(player, "mnd"));
			case "dbcspi":
				return NumberFormats.formatInt(dbcManager.getStat(player, "spi"));
			case "dbclvl":
				int level = (
						(dbcManager.getStat(player, "str") + dbcManager.getStat(player, "dex") +
								dbcManager.getStat(player, "con") + dbcManager.getStat(player, "wil") +
								dbcManager.getStat(player, "mnd") + dbcManager.getStat(player, "spi")) / 5 - 11
				);

				String prefix = plugin.getLanguage().getRawMessage("placeholder.level_prefix");
				return prefix + NumberFormats.formatInt(level);

			case "global_boost":
				return formatHighestBoost(boostManager.getActiveGlobalBoosts());
			case "personal_boost":
				DBSPlayer dbsPlayer = plugin.getPlayerManager().getDBSPlayer(player);
				return formatHighestBoost(dbsPlayer != null ? dbsPlayer.getActiveBoosts() : Collections.<ActiveBoost>emptyList());
			case "rank_boost":
				return formatHighestBoost(boostManager.getActiveRankBoosts(player));
			case "total_boost":
				double total = boostManager.getFinalBoost(player);
				if (total <= 1.0) {
					return plugin.getLanguage().getRawMessage("placeholder.boost_inactive");
				}
				int percent = (int) Math.round((total - 1.0) * 100);
				return percent + "%";

			default:
				return null;
		}
	}

	private String formatHighestBoost(List<ActiveBoost> boosts) {
		if (boosts == null || boosts.isEmpty()) {
			return plugin.getLanguage().getRawMessage("placeholder.boost_inactive");
		}

		ActiveBoost highestBoost = null;
		for (ActiveBoost currentBoost : boosts) {
			if (highestBoost == null || currentBoost.getAmount() > highestBoost.getAmount()) {
				highestBoost = currentBoost;
			}
		}

		if (highestBoost == null) {
			return plugin.getLanguage().getRawMessage("placeholder.boost_inactive");
		}

		String amount = String.format("%.0f%%", highestBoost.getAmount());
		if (highestBoost.getDuration() == -1) {
			return amount;
		}

		long remainingSeconds = 0;
		if (highestBoost.getExpirationTimestamp() != -1) {
			remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(highestBoost.getExpirationTimestamp() - System.currentTimeMillis());
		} else {
			remainingSeconds = highestBoost.getDuration();
		}

		String time = NumberFormats.formatDuration(remainingSeconds);
		return amount + " - " + time;
	}
}