package com.shokkoh.dbsparking.commands.subcommands.boost;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.boosters.ActiveBoost;
import com.shokkoh.dbsparking.boosters.BoostType;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.entities.DBSPlayer;
import com.shokkoh.dbsparking.utils.NumberFormats;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BoostAddCommand extends SubCommand {
	@Override
	public String getName() {
		return "add";
	}

	@Override
	public String getDescription() {
		return "Adds a new active booster.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp boost add <type> <name> <target> <amount> <time> [author]";
	}

	@Override
	public Permissions getPermission() { return Permissions.BOOST_ADD; }

	@Override
	public void perform(CommandSender sender, String[] args) {
		// Formatos:
		// /dbsp boost add global <name> <amount> <time> [author]
		// /dbsp boost add personal <name> <player> <amount> <time> [author]
		// /dbsp boost add rank <name> <rank> <amount> <time> [author]
		if (args.length < 5) {
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		BoostType type;
		try {
			type = BoostType.valueOf(args[2].toUpperCase());
		} catch (IllegalArgumentException e) {
			sender.sendMessage(plugin.getLanguage().getMessage("invalid_boost_type"));
			return;
		}

		ActiveBoost boost = new ActiveBoost();
		boost.setBoostType(type);

		String name;
		String target;
		String targetNameForBoost = null;
		double amount;
		long duration;
		String author;
		UUID targetUUID = null;

		try {
			switch (type) {
				case GLOBAL:
					if (args.length < 6) {
						sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", "/dbsp boost add global <name> <amount> <time> [author]"));
						return;
					}
					name = args[3];
					target = "global";
					amount = Double.parseDouble(args[4]);
					String timeStrGlobal = args[5];
					duration = (timeStrGlobal.equalsIgnoreCase("permanent") || timeStrGlobal.equals("-1")) ? -1 : Long.parseLong(timeStrGlobal);
					author = (args.length > 6) ? args[6] : (sender instanceof Player ? sender.getName() : "Console");
					break;
				case PERSONAL:
				case RANK:
					if (args.length < 7) {
						sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", "/dbsp boost add " + type.name().toLowerCase() + " <name> <target> <amount> <time> [author]"));
						return;
					}
					name = args[3];
					String targetNameInput = args[4];
					amount = Double.parseDouble(args[5]);
					String timeStr = args[6];
					duration = (timeStr.equalsIgnoreCase("permanent") || timeStr.equals("-1")) ? -1 : Long.parseLong(timeStr);
					author = (args.length > 7) ? args[7] : (sender instanceof Player ? sender.getName() : "Console");

					if (type == BoostType.PERSONAL) {
						Player targetPlayer = Bukkit.getPlayer(targetNameInput);
						if (targetPlayer == null || !targetPlayer.isOnline()) {
							sender.sendMessage(plugin.getLanguage().getMessage("player_not_online").replace("%player%", targetNameInput));
							return;
						}
						targetUUID = targetPlayer.getUniqueId();
						target = targetUUID.toString();
						targetNameForBoost = targetPlayer.getName();
					} else {
						target = targetNameInput;
					}
					break;
				default:
					return;
			}

			if (type == BoostType.PERSONAL) {
				if (plugin.getBoostDataManager().personalBoostExists(targetUUID, name)) {
					sender.sendMessage("§cPlayer " + targetNameForBoost + " already has a personal boost with the name '" + name + "'.");
					return;
				}
			} else {
				if (plugin.getBoostDataManager().boostExists(name)) {
					sender.sendMessage(plugin.getLanguage().getMessage("boost_add_already_exists").replace("%name%", name));
					return;
				}
			}

			boost.setName(name);
			boost.setTarget(target);
			boost.setTargetName(targetNameForBoost);
			boost.setAmount(amount);
			boost.setAuthor(author);
			boost.setDuration(duration);
			boost.setLastUpdated(new Date());

		} catch (NumberFormatException e) {
			sender.sendMessage(plugin.getLanguage().getMessage("invalid_number"));
			return;
		}

		int newBoostId = plugin.getBoostDataManager().addBoost(boost);

		if (newBoostId != -1) {
			boost.setId(newBoostId);

			sender.sendMessage(plugin.getLanguage().getMessage("boost_add_success")
					.replace("%name%", boost.getName())
					.replace("%type%", boost.getBoostType().name()));

			handleNotifications(boost, duration);

			if (boost.getBoostType() == BoostType.PERSONAL) {
				Player targetPlayer = Bukkit.getPlayer(UUID.fromString(boost.getTarget()));
				if (targetPlayer != null && targetPlayer.isOnline()) {
					DBSPlayer dbsPlayer = plugin.getPlayerManager().getDBSPlayer(targetPlayer);
					if (dbsPlayer != null) {
						if (boost.getDuration() != -1) {
							boost.setExpirationTimestamp(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(boost.getDuration()));
						}
						dbsPlayer.getActiveBoosts().add(boost);
					}
				}
			} else {
				plugin.getBoostManager().loadBoostsFromDatabase();
			}

		} else {
			sender.sendMessage(plugin.getLanguage().getMessage("boost_add_error"));
		}
	}

	private void handleNotifications(ActiveBoost boost, long timeInSeconds) {
		Sound receiveSound = Sound.valueOf(plugin.getConfig().getString("sounds.boost_receive", "LEVEL_UP"));
		Sound globalSound = Sound.valueOf(plugin.getConfig().getString("sounds.global_boost_activate", "ENDERDRAGON_GROWL"));

		switch (boost.getBoostType()) {
			case GLOBAL:
				String broadcastMsg = plugin.getLanguage().getMessage("global_boost_broadcast")
						.replace("%author%", boost.getAuthor())
						.replace("%amount%", String.format("%.0f", boost.getAmount()))
						.replace("%time%", String.valueOf(timeInSeconds))
						.replace("%name%", boost.getName());

				Bukkit.broadcastMessage(broadcastMsg);

				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					p.playSound(p.getLocation(), globalSound, 1.0f, 1.0f);
				}
				break;

			case PERSONAL:
				Player target = Bukkit.getPlayer(UUID.fromString(boost.getTarget()));
				if (target != null && target.isOnline()) {
					target.playSound(target.getLocation(), receiveSound, 1.0f, 1.0f);
					String timeStr = (timeInSeconds == -1) ? "Permanent" : NumberFormats.formatDuration(timeInSeconds);
					target.sendMessage(plugin.getLanguage().getMessage("boost_personal_received")
							.replace("%name%", boost.getName())
							.replace("%time%", timeStr));
				}
				break;

			case RANK:
				String rankName = boost.getTarget();
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					Player freshPlayer = Bukkit.getServer().getPlayer(p.getName());
					if (DBSparking.perms != null && freshPlayer != null && DBSparking.perms.playerInGroup(freshPlayer, rankName)) {
						p.playSound(p.getLocation(), receiveSound, 1.0f, 1.0f);
					}
				}
				break;
		}
	}
}
