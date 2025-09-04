package com.shokkoh.dbsparking.commands.subcommands.boost;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.boosters.ActiveBoost;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.entities.DBSPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Iterator;
import java.util.UUID;

public class BoostDeleteCommand extends SubCommand {
	@Override
	public String getName() {
		return "delete";
	}

	@Override
	public String getDescription() {
		return "Delete an existing active booster by name.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp boost delete <name> [player_that_owns_it]";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.BOOST_DELETE;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (args.length < 3) {
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		String boostName = args[2];

		if (args.length >= 4) {
			String playerName = args[3];

			org.bukkit.entity.Player online = Bukkit.getPlayerExact(playerName);
			UUID targetUUID = null;

			if (online != null) {
				targetUUID = online.getUniqueId();

				DBSPlayer dbsPlayer = plugin.getPlayerManager().getDBSPlayer(online);
				if (dbsPlayer != null) {
					Iterator<ActiveBoost> it = dbsPlayer.getActiveBoosts().iterator();
					while (it.hasNext()) {
						ActiveBoost b = it.next();
						if (b.getName().equalsIgnoreCase(boostName)) {
							it.remove();
							break;
						}
					}
				}
			} else {
				OfflinePlayer off = Bukkit.getOfflinePlayer(playerName);
				if (off == null || (!off.hasPlayedBefore() && !off.isOnline())) {
					sender.sendMessage(plugin.getLanguage().getMessage("player_not_online").replace("%player%", playerName));
					return;
				}
				targetUUID = off.getUniqueId();
			}

			if (targetUUID == null) {
				sender.sendMessage(plugin.getLanguage().getMessage("player_not_online").replace("%player%", playerName));
				return;
			}

			boolean deletedFromDB = plugin.getBoostDataManager().deletePersonalBoost(targetUUID, boostName);
			if (deletedFromDB) {
				sender.sendMessage(plugin.getLanguage().getMessage("boost_delete_success").replace("%name%", boostName));
			} else {
				sender.sendMessage("§cError: Boost not found for player " + playerName + " with name '" + boostName + "'.");
			}
			return;
		}

		ActiveBoost boostToDelete = plugin.getBoostManager().getBoostFromCache(boostName);

		if (boostToDelete != null) {
			int boostId = boostToDelete.getId();
			boolean deletedFromDB = plugin.getBoostDataManager().deleteBoostById(boostId);

			if (deletedFromDB) {
				plugin.getBoostManager().removeBoostFromCache(boostId);
				sender.sendMessage(plugin.getLanguage().getMessage("boost_delete_success").replace("%name%", boostName));
			} else {
				sender.sendMessage("§cError: Boost found in cache but could not be deleted from database. Please contact an administrator.");
			}
			return;
		}

		sender.sendMessage(plugin.getLanguage().getMessage("boost_delete_not_found").replace("%name%", boostName));
	}
}