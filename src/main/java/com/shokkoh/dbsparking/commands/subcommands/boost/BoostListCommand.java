package com.shokkoh.dbsparking.commands.subcommands.boost;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.boosters.ActiveBoost;
import com.shokkoh.dbsparking.boosters.BoostType;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.entities.DBSPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BoostListCommand extends SubCommand {

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public String getDescription() {
		return "Shows the list of active boosters.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp boost list";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.BOOST_LIST;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		List<ActiveBoost> boostsToList = new ArrayList<>();

		boostsToList.addAll(plugin.getBoostManager().getNonPersonalBoostsCache());

		List<ActiveBoost> allDbBoosts = plugin.getBoostDataManager().getActiveBoosts();
		for (ActiveBoost dbBoost : allDbBoosts) {
			if (dbBoost.getBoostType() == BoostType.PERSONAL) {
				boostsToList.add(dbBoost);
			}
		}

		if (boostsToList.isEmpty()) {
			sender.sendMessage(plugin.getLanguage().getMessage("boost_list_empty"));
			return;
		}

		sender.sendMessage(plugin.getLanguage().getRawMessage("boost_list_header"));
		for (ActiveBoost boost : boostsToList) {
			String remainingTime = calculateRemainingTime(boost);

			String format = plugin.getLanguage().getRawMessage("boost_list_format")
					.replace("%name%", boost.getName())
					.replace("%type%", boost.getBoostType().name())
					.replace("%target%", getTargetName(boost))
					.replace("%amount%", String.format("%.0f", boost.getAmount()))
					.replace("%expires%", remainingTime);
			sender.sendMessage(format);
		}
	}

	private String getTargetName(ActiveBoost boost) {
		if (boost.getBoostType() == BoostType.PERSONAL) {
			return boost.getTargetName() != null ? boost.getTargetName() : "N/A";
		}
		return boost.getTarget();
	}

	private String calculateRemainingTime(ActiveBoost boost) {
		if (boost.getDuration() == -1) {
			return "Permanent";
		}

		if (boost.getBoostType() == BoostType.PERSONAL) {
			try {
				UUID uuid = UUID.fromString(boost.getTarget());
				Player target = Bukkit.getPlayer(uuid);
				if (target != null && target.isOnline()) {
					DBSPlayer dbsPlayer = plugin.getPlayerManager().getDBSPlayer(target);
					if (dbsPlayer != null) {
						for (ActiveBoost sessionBoost : dbsPlayer.getActiveBoosts()) {
							if (sessionBoost.getId() == boost.getId()) {
								long remainingMillis = sessionBoost.getExpirationTimestamp() - System.currentTimeMillis();
								return formatDuration(TimeUnit.MILLISECONDS.toSeconds(remainingMillis));
							}
						}
					}
				}
				return formatDuration(boost.getDuration()) + " (paused)";
			} catch (IllegalArgumentException e) {
				return "Invalid Target";
			}
		} else {
			long creationTime = boost.getLastUpdated().getTime();
			long durationMillis = TimeUnit.SECONDS.toMillis(boost.getDuration());
			long expectedEndTime = creationTime + durationMillis;
			long remainingMillis = expectedEndTime - System.currentTimeMillis();
			return formatDuration(TimeUnit.MILLISECONDS.toSeconds(remainingMillis));
		}
	}

	private String formatDuration(long totalSeconds) {
		if (totalSeconds <= 0) return "Expired";
		long hours = TimeUnit.SECONDS.toHours(totalSeconds);
		long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60;
		long seconds = totalSeconds % 60;

		if (hours >= 1) {
			return hours + "h";
		} else if (minutes >= 1) {
			return minutes + "m " + seconds + "s";
		} else {
			return seconds + "s";
		}
	}
}