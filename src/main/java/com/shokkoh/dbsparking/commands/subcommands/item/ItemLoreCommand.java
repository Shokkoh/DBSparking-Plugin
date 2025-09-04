package com.shokkoh.dbsparking.commands.subcommands.item;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.items.StatsItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ItemLoreCommand extends SubCommand {
	@Override
	public String getName() {
		return "lore";
	}

	@Override
	public String getDescription() {
		return "Edit the lore of a custom item.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp item lore <internalName> <line> <action>";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.ITEM_LORE;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cYou can't use this command from the console.");
			return;
		}
		if (args.length < 5) {
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		Player player = (Player) sender;
		String internalName = args[2];
		int line;
		try {
			line = Integer.parseInt(args[3]);
			if (line < 1 || line > 6) throw new NumberFormatException();
		} catch (NumberFormatException e) {
			player.sendMessage(plugin.getLanguage().getMessage("item_lore_invalid_line"));
			return;
		}

		String action = args[4].toLowerCase();
		StatsItem item = plugin.getItemDataManager().getItemByName(internalName);
		if (item == null) {
			player.sendMessage(plugin.getLanguage().getMessage("item_not_found").replace("%name%", internalName));
			return;
		}

		if (action.equals("add") || action.equals("edit")) {
			plugin.getItemManager().setPlayerLoreEditing(player, item, line);
			player.sendMessage(plugin.getLanguage().getMessage("item_lore_prompt").replace("%line%", String.valueOf(line)));
		} else if (action.equals("delete") || action.equals("remove")) {
			player.sendMessage(plugin.getLanguage().getMessage("item_lore_success").replace("%name%", internalName));
		} else {
			player.sendMessage(plugin.getLanguage().getMessage("item_lore_invalid_action"));
		}
	}
}
