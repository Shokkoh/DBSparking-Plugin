package com.shokkoh.dbsparking.commands.subcommands;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.inventories.SoulsInventory;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SoulCommand extends SubCommand {

	@Override
	public String getName() {
		return "souls";
	}

	@Override
	public String getDescription() {
		return "Open your Z-Souls inventory.";
	}

	@Override
	public String getSyntax() {
		return "/dbs souls";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.SOUL_INVENTORY;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cConsole cannot use this command.");
			return;
		}

		Player viewer = (Player) sender;

		if (args.length >= 2) {
			if (!Permissions.ADMIN.has(sender)) {
				sender.sendMessage(plugin.getLanguage().getMessage("no_permission"));
				return;
			}
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null || !target.isOnline()) {
				sender.sendMessage(plugin.getLanguage().getMessage("player_not_online").replace("%player%", args[1]));
				return;
			}
			new SoulsInventory(target, viewer).open();
			sender.sendMessage(plugin.getLanguage().getMessage("soul_inventory_open_other").replace("%player%", target.getName()));
			viewer.playSound(viewer.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
			return;
		}

		new SoulsInventory(viewer).open();
		viewer.sendMessage(plugin.getLanguage().getMessage("soul_inventory_open"));
		viewer.playSound(viewer.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
	}
}
