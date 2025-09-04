package com.shokkoh.dbsparking.commands.subcommands.item;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.items.StatsItem;
import com.shokkoh.dbsparking.utils.Text;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ItemInfoCommand extends SubCommand {
	@Override
	public String getName() {
		return "info";
	}

	@Override
	public String getDescription() {
		return "Shows detailed stats for a custom item.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp item info <internalName>";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.ITEM_LIST;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (args.length < 3) {
			sender.sendMessage("§cItem name required.");
			return;
		}

		String internalName = args[2];
		StatsItem item = plugin.getItemDataManager().getItemByName(internalName);
		if (item == null) {
			sender.sendMessage(plugin.getLanguage().getMessage("item_not_found").replace("%name%", internalName));
			return;
		}

		// Generamos el lore de stats usando los mismos métodos que en ItemDataManager
		List<String> statLore = plugin.getItemDataManager().getStatLoreFor(item);

		if (statLore.isEmpty()) {
			sender.sendMessage(Text.color("&cThe item &e'" + internalName + "'&c has no stats."));
			return;
		}

		// Mostramos la información formateada
		sender.sendMessage(Text.color(String.format("&6ID: %s &7| %s &6- Stats",
				item.getInternalName(),
				item.getDisplayName()
		)));

		for (String line : statLore) {
			sender.sendMessage(line);
		}
	}
}