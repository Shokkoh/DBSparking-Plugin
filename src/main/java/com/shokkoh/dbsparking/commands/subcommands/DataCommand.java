package com.shokkoh.dbsparking.commands.subcommands;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.commands.subcommands.data.DataConfirmCommand;
import com.shokkoh.dbsparking.commands.subcommands.data.DataDeleteCommand;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataCommand extends SubCommand {
	private final Map<String, SubCommand> subCommands = new HashMap<>();
	private final Map<UUID, PendingDeletion> pendingDeletions = new HashMap<>();

	public DataCommand() {
		addSubCommand(new DataDeleteCommand(this));
		addSubCommand(new DataConfirmCommand(this));
	}

	private void addSubCommand(SubCommand command) {
		subCommands.put(command.getName().toLowerCase(), command);
	}

	@Override
	public String getName() {
		return "data";
	}

	@Override
	public String getDescription() {
		return "Manages player data from other mods (DBC and CNPC).";
	}

	@Override
	public String getSyntax() {
		return "/dbsp data <subcommand>";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.DATA_MANAGE;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage("§cYou must specify an action: delete, confirm.");
			return;
		}
		SubCommand subCommand = subCommands.get(args[1].toLowerCase());
		if (subCommand != null) {
			subCommand.perform(sender, args);
		} else {
			sender.sendMessage(plugin.getLanguage().getMessage("invalid_action"));
		}
	}

	public void addPendingDeletion(UUID senderUUID, PendingDeletion deletion) {
		pendingDeletions.put(senderUUID, deletion);
	}

	public PendingDeletion retrievePendingDeletion(UUID senderUUID) {
		return pendingDeletions.remove(senderUUID);
	}

	public static class PendingDeletion {
		public final UUID targetUUID;
		public final String targetName;
		public final String dataType;
		public final long timestamp;

		public PendingDeletion(UUID targetUUID, String targetName, String dataType) {
			this.targetUUID = targetUUID;
			this.targetName = targetName;
			this.dataType = dataType;
			this.timestamp = System.currentTimeMillis();
		}
	}
}
