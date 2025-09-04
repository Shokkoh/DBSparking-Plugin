package com.shokkoh.dbsparking.commands.subcommands.item;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.items.StatsItem;
import org.bukkit.command.CommandSender;

public class ItemConditionCommand extends SubCommand {
	@Override
	public String getName() {
		return "condition";
	}

	@Override
	public String getDescription() {
		return "Sets the equip conditions for an item.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp item condition <internalName> <add|remove> <level|rank> [options]";
	}

	@Override public Permissions getPermission() {
		return Permissions.ITEM_CONDITION;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (args.length < 4) { // Mínimo: /dbsp item condition <name> <remove>
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		String internalName = args[2];
		StatsItem item = plugin.getItemDataManager().getItemByName(internalName);
		if (item == null) {
			sender.sendMessage(plugin.getLanguage().getMessage("item_not_found").replace("%name%", internalName));
			return;
		}

		String action = args[3].toLowerCase();

		if (action.equals("remove")) {
			plugin.getItemDataManager().updateItemConditions(item.getId(), 0, -1, null);
			sender.sendMessage(plugin.getLanguage().getMessage("item_condition_success").replace("%name%", internalName));
			return;
		}

		if (args.length < 5) { // 'add' requiere más argumentos
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		String conditionType = args[4].toLowerCase();

		if (action.equals("add")) {
			if (conditionType.equals("level")) {
				int minLvl = item.getMinLevel(); // Empezamos con los valores actuales
				int maxLvl = item.getMaxLevel();

				// Bucle para parsear min:X y max:Y de forma flexible
				for (int i = 5; i < args.length; i++) {
					try {
						String[] parts = args[i].split(":");
						if (parts.length == 2) {
							if (parts[0].equalsIgnoreCase("min")) {
								minLvl = Integer.parseInt(parts[1]);
							} else if (parts[0].equalsIgnoreCase("max")) {
								maxLvl = Integer.parseInt(parts[1]);
							}
						}
					} catch (NumberFormatException e) {
						sender.sendMessage(plugin.getLanguage().getMessage("invalid_number"));
						return;
					}
				}
				plugin.getItemDataManager().updateItemConditions(item.getId(), minLvl, maxLvl, item.getRequiredRank());
				sender.sendMessage(plugin.getLanguage().getMessage("item_condition_success").replace("%name%", internalName));

			} else if (conditionType.equals("rank")) {
				if (args.length < 6) {
					sender.sendMessage(plugin.getLanguage().getMessage("item_condition_no_rank"));
					return;
				}
				String rankName = args[5];
				plugin.getItemDataManager().updateItemConditions(item.getId(), item.getMinLevel(), item.getMaxLevel(), rankName);
				sender.sendMessage(plugin.getLanguage().getMessage("item_condition_success").replace("%name%", internalName));
			} else {
				sender.sendMessage(plugin.getLanguage().getMessage("item_condition_invalid"));
			}
		} else {
			sender.sendMessage(plugin.getLanguage().getMessage("item_condition_invalid"));
		}
	}
}