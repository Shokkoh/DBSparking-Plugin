package com.shokkoh.dbsparking.commands.subcommands;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.inventories.CustomItemGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CustomItemCommand extends SubCommand {
	@Override
	public String getName() {
		return "customitem";
	}

	@Override
	public String getDescription() {
		return "Opens the custom item management GUI.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp customitem";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.ITEM_CREATE;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be used by players.");
			return;
		}
		new CustomItemGUI((Player) sender, 0).open();
	}
}