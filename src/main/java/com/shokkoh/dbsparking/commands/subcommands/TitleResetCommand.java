package com.shokkoh.dbsparking.commands.subcommands;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.utils.TitleReflection;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TitleResetCommand extends SubCommand {

	@Override
	public String getName() {
		return "titlereset";
	}

	@Override
	public String getDescription() {
		return "Resets the title on a player's screen.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp titlereset [player]";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.TITLE_SEND;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		String objective = "";

		if (args.length >= 2) {
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null || !target.isOnline()) {
				sender.sendMessage(plugin.getLanguage().getMessage("player_not_online").replace("%player%", args[1]));
				return;
			}
			TitleReflection.resetTitle(target);
			objective = target.getName();
		} else {
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				TitleReflection.resetTitle(p);
			}
			objective = "@a";
		}

		sender.sendMessage(plugin.getLanguage().getMessage("delete_title_data").replace("%player%", objective));
	}
}
