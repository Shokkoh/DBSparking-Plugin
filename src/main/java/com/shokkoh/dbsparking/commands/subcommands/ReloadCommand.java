package com.shokkoh.dbsparking.commands.subcommands;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {
	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public String getDescription() {
		return "Reloads plugin's configuration.";
	}

	@Override
	public String getSyntax() {
		return "/dbs reload";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.RELOAD;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		plugin.reloadConfig();
		plugin.getLanguage().reload();
		plugin.getBoostManager().loadBoostsFromDatabase();
		plugin.getItemConfigManager().loadItems();
		plugin.getAutoLoginManager().load();

		sender.sendMessage(plugin.getLanguage().getMessage("reload_success"));
	}
}