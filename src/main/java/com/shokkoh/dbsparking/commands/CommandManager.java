package com.shokkoh.dbsparking.commands;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.commands.subcommands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor {

	private final List<SubCommand> subCommands = new ArrayList<>();
	private final DBSparking plugin = DBSparking.getInstance();

	public CommandManager() {
		subCommands.add(new HelpCommand(this));
		subCommands.add(new ReloadCommand());
		subCommands.add(new TPSCommand());
		subCommands.add(new BoostCommand());
		subCommands.add(new ItemCommand());
		subCommands.add(new DataCommand());
		subCommands.add(new SoulCommand());
		subCommands.add(new TitleCommand());
		subCommands.add(new SubtitleCommand());
		subCommands.add(new TitleResetCommand());
		subCommands.add(new CustomItemCommand());
		subCommands.add(new ExecutePlayerCommand());
		subCommands.add(new AutoLoginCommand());
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
			getSubCommand("help").perform(sender, args);
			return true;
		}

		SubCommand subCommand = getSubCommand(args[0]);
		if (subCommand == null) {
			sender.sendMessage(plugin.getLanguage().getMessage("unknown_command"));
			return true;
		}

		if (!subCommand.getPermission().has(sender)) {
			sender.sendMessage(plugin.getLanguage().getMessage("no_permission"));
			return true;
		}

		subCommand.perform(sender, args);
		return true;
	}

	public List<SubCommand> getSubCommands() {
		return subCommands;
	}

	public SubCommand getSubCommand(String name) {
		for (SubCommand subCommand : subCommands) {
			if (subCommand.getName().equalsIgnoreCase(name)) {
				return subCommand;
			}
		}
		return null;
	}
}