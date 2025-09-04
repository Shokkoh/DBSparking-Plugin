package com.shokkoh.dbsparking.commands.subcommands.item;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.items.StatsItem;
import com.shokkoh.dbsparking.items.ItemType;
import com.shokkoh.dbsparking.items.Rarity;
import org.bukkit.command.CommandSender;

public class ItemEditCommand extends SubCommand {
	@Override
	public String getName() {
		return "edit";
	}

	@Override
	public String getDescription() {
		return "Edits an existing custom item with optional arguments.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp item edit <internalName> [property:value]...";
	}

	@Override public Permissions getPermission() {
		return Permissions.ITEM_EDIT;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (args.length < 4) {
			String correctSyntax = "§cSyntax: /dbsp item edit <internalName> [name:newName] [type:armor|weapon|soul] [display:\"New Display Name\"] [rarity:newRarity]";
			sender.sendMessage(plugin.getLanguage().getMessage("incorrect_syntax").replace("%syntax%", correctSyntax));
			return;
		}

		String internalName = args[2];
		StatsItem item = plugin.getItemDataManager().getItemByName(internalName);
		if (item == null) {
			sender.sendMessage(plugin.getLanguage().getMessage("item_not_found").replace("%name%", internalName));
			return;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 3; i < args.length; i++) {
			sb.append(args[i]).append(" ");
		}
		String fullArgs = sb.toString().trim();

		// Parsear los argumentos clave:valor
		try {
			// Unir argumentos entre comillas para el display name
			String[] parts = fullArgs.split("\"");
			if (parts.length > 1) {
				item.setDisplayName(parts[1]);
				fullArgs = parts[0] + (parts.length > 2 ? parts[2] : "");
			}

			for (String arg : fullArgs.split(" ")) {
				if (!arg.contains(":")) continue;
				String[] keyValue = arg.split(":", 2);
				String key = keyValue[0].toLowerCase();
				String value = keyValue[1];

				switch (key) {
					case "name":
						if (plugin.getItemDataManager().itemExists(value)) {
							sender.sendMessage(plugin.getLanguage().getMessage("item_create_already_exists").replace("%name%", value));
							return;
						}
						item.setInternalName(value);
						break;
					case "type":
						item.setItemType(ItemType.valueOf(value.toUpperCase()));
						break;
					case "rarity":
						try {
							item.setRarity(Rarity.valueOf(value.toUpperCase()));
						} catch (IllegalArgumentException e) {
							sender.sendMessage("§cInvalid rarity. Use: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY");
						}
						break;
				}
			}
		} catch (Exception e) {
			sender.sendMessage("§cError parsing arguments. Make sure the format is correct.");
			return;
		}

		plugin.getItemDataManager().updateItem(internalName, item);
		sender.sendMessage(plugin.getLanguage().getMessage("item_edit_success").replace("%name%", item.getInternalName()));
	}
}