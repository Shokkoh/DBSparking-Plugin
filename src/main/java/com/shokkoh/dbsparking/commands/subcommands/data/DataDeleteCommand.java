package com.shokkoh.dbsparking.commands.subcommands.data;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.commands.subcommands.DataCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DataDeleteCommand extends SubCommand {

	private final DataCommand parent;

	public DataDeleteCommand(DataCommand parent) {
		this.parent = parent;
	}

	@Override
	public String getName() {
		return "delete";
	}
	@Override
	public String getDescription() {
		return "Initiates a request to delete player data.";
	}
	@Override
	public String getSyntax() {
		return "/dbsp data delete <player> <dbc|npc|all>";
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
		if (args.length < 4) {
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		Player requester = (Player) sender;
		String playerName = args[2];
		String dataType = args[3].toLowerCase();

		if (!dataType.equals("dbc") && !dataType.equals("npc") && !dataType.equals("all")) {
			sender.sendMessage(plugin.getLanguage().getMessage("data_invalid_type"));
			return;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
		if (target == null || !target.hasPlayedBefore()) {
			sender.sendMessage(plugin.getLanguage().getMessage("player_not_online").replace("%player%", playerName));
			return;
		}

		DataCommand.PendingDeletion pendingDeletion = new DataCommand.PendingDeletion(target.getUniqueId(), target.getName(), dataType);
		parent.addPendingDeletion(requester.getUniqueId(), pendingDeletion);

		sender.sendMessage(plugin.getLanguage().getMessage("data_delete_confirmation")
				.replace("%type%", dataType)
				.replace("%player%", target.getName()));
	}
}
