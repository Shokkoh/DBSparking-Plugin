package com.shokkoh.dbsparking.commands.subcommands.data;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.commands.subcommands.DataCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class DataConfirmCommand extends SubCommand {

	private final DataCommand parent;

	public DataConfirmCommand(DataCommand parent) {
		this.parent = parent;
	}

	@Override
	public String getName() {
		return "confirm";
	}

	@Override
	public String getDescription() {
		return "Confirms a pending data deletion.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp data confirm";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.DATA_MANAGE;
	}


	@Override
	public void perform(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cThis command can only be used by a player.");
			return;
		}

		Player requester = (Player) sender;
		DataCommand.PendingDeletion request = parent.retrievePendingDeletion(requester.getUniqueId());

		if (request == null) {
			sender.sendMessage(plugin.getLanguage().getMessage("data_delete_no_pending"));
			return;
		}

		long secondsPassed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - request.timestamp);
		if (secondsPassed > 10) {
			sender.sendMessage(plugin.getLanguage().getMessage("data_delete_expired"));
			return;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(request.targetUUID);
		if (target.isOnline()) {
			target.getPlayer().kickPlayer(plugin.getLanguage().getRawMessage("data_delete_kick_message"));
		}

		boolean success = plugin.getDataFetcherManager().deletePlayerData(request.dataType, request.targetUUID);

		if (success) {
			sender.sendMessage(plugin.getLanguage().getMessage("data_delete_success")
					.replace("%type%", request.dataType)
					.replace("%player%", request.targetName));
		} else {
			sender.sendMessage(plugin.getLanguage().getMessage("data_delete_failed"));
		}
	}
}