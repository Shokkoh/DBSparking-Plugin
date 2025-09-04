package com.shokkoh.dbsparking.commands.subcommands;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.CommandManager;
import com.shokkoh.dbsparking.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class HelpCommand extends SubCommand {
	private final CommandManager commandManager;

	public HelpCommand(CommandManager commandManager) {
		this.commandManager = commandManager;
	}

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public String getDescription() {
		return "Shows the list of all/specific available commands.";
	}

	@Override
	public String getSyntax() {
		return "/dbs help [boost|item]";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.HELP;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		sender.sendMessage(plugin.getLanguage().getRawMessage("boost_list_header").replace("Active Boosters List", "DBSparking Commands"));

		if (args.length < 3) {
			displayHelpRecursive(sender, commandManager.getSubCommands(), "");
			return;
		}

		String commandName = args[2].toLowerCase();
		SubCommand parentCommand = commandManager.getSubCommand(commandName);

		if (parentCommand == null) {
			sender.sendMessage("§cCommand '" + commandName + "' not found.");
			return;
		}

		try {
			java.lang.reflect.Method getSubCommandsMethod = parentCommand.getClass().getMethod("getSubCommands");
			Map<String, SubCommand> nestedSubCommands = (Map<String, SubCommand>) getSubCommandsMethod.invoke(parentCommand);
			displayHelpRecursive(sender, nestedSubCommands.values(), parentCommand.getName() + " ");
		} catch (Exception e) {
			if (parentCommand.getPermission().has(sender)) {
				sender.sendMessage("§6" + parentCommand.getSyntax() + " §7- " + parentCommand.getDescription());
			}
		}
	}

	/**
	 * Un métod recursivo para mostrar la ayuda de los comandos y sus subcomandos.
	 */
	private void displayHelpRecursive(CommandSender sender, Iterable<SubCommand> commands, String prefix) {
		for (SubCommand subCommand : commands) {
			if (subCommand.getPermission().has(sender)) {
				sender.sendMessage("§6" + subCommand.getSyntax() + " §7- " + subCommand.getDescription());

				try {
					java.lang.reflect.Method getSubCommandsMethod = subCommand.getClass().getMethod("getSubCommands");
					Map<String, SubCommand> nestedSubCommands = (Map<String, SubCommand>) getSubCommandsMethod.invoke(subCommand);
					if (!nestedSubCommands.isEmpty()) {
						displayHelpRecursive(sender, nestedSubCommands.values(), subCommand.getName() + " ");
					}
				} catch (Exception ignored) {
				}
			}
		}
	}
}