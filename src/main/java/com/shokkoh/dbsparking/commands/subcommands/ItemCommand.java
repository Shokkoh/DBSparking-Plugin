package com.shokkoh.dbsparking.commands.subcommands;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.commands.subcommands.item.*;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class ItemCommand extends SubCommand {
	private final Map<String, SubCommand> subCommands = new HashMap<>();

	public ItemCommand() {
		addSubCommand(new ItemListCommand());
		addSubCommand(new ItemInfoCommand());
		addSubCommand(new ItemCreateCommand());
		addSubCommand(new ItemDeleteCommand());
		addSubCommand(new ItemEditCommand());
		addSubCommand(new ItemGiveCommand());
		addSubCommand(new ItemStatCommand());
		addSubCommand(new ItemConditionCommand());
		addSubCommand(new ItemLoreCommand());
		addSubCommand(new ItemFinalLoreCommand());
	}

	private void addSubCommand(SubCommand command) {
		subCommands.put(command.getName().toLowerCase(), command);
	}

	public Map<String, SubCommand> getSubCommands() {
		return subCommands;
	}

	@Override
	public String getName() {
		return "item";
	}

	@Override
	public String getDescription() {
		return "Manage custom DBSparking items.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp item <subcommand>";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.ITEM_LIST;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(plugin.getLanguage().getMessage("specify_action_item"));
			return;
		}

		String subCommandName = args[1].toLowerCase();
		SubCommand subCommand = subCommands.get(subCommandName);
		if (subCommand != null) {
			if (subCommand.getName().equalsIgnoreCase(subCommandName)) {
				if (!subCommand.getPermission().has(sender)) {
					sender.sendMessage(plugin.getLanguage().getMessage("no_permission"));
					return;
				}
				subCommand.perform(sender, args);
				return;
			}
		}
		sender.sendMessage(plugin.getLanguage().getMessage("specify_action_item"));
	}
}
