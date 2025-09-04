package com.shokkoh.dbsparking.commands.subcommands;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.commands.subcommands.boost.BoostAddCommand;
import com.shokkoh.dbsparking.commands.subcommands.boost.BoostDeleteCommand;
import com.shokkoh.dbsparking.commands.subcommands.boost.BoostListCommand;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class BoostCommand extends SubCommand {
	private final Map<String, SubCommand> subCommands = new HashMap<>();

	public BoostCommand() {
		addSubCommand(new BoostListCommand());
		addSubCommand(new BoostAddCommand());
		addSubCommand(new BoostDeleteCommand());
	}

	private void addSubCommand(SubCommand command) {
		subCommands.put(command.getName().toLowerCase(), command);
	}

	public Map<String, SubCommand> getSubCommands() {
		return subCommands;
	}

	@Override
	public String getName() {
		return "boost";
	}

	@Override
	public String getDescription() {
		return "Manage active boosters.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp boost <subcommand>";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.BOOST_LIST;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(plugin.getLanguage().getMessage("specify_action_boost"));
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
		sender.sendMessage(plugin.getLanguage().getMessage("specify_action_boost"));
	}
}