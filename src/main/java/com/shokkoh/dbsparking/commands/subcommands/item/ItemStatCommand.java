package com.shokkoh.dbsparking.commands.subcommands.item;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.items.StatsItem;
import com.shokkoh.dbsparking.items.ModifierType;
import com.shokkoh.dbsparking.items.Stat;
import com.shokkoh.dbsparking.items.StatBonus;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class ItemStatCommand extends SubCommand {
	@Override
	public String getName() {
		return "stat";
	}

	@Override
	public String getDescription() {
		return "Adds, multiplies, or removes a stat from a custom item.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp item stat <internalName> <stat> <add|multiply|divide|percent|remove> [value] \"[bonusID]\"";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.ITEM_STAT;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		// Sintaxis: /dbsp item stat <name> <stat> <type> [value]
		if (args.length < 5) {
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		String internalName = args[2];
		StatsItem item = plugin.getItemDataManager().getItemByName(internalName);
		if (item == null) {
			sender.sendMessage(plugin.getLanguage().getMessage("item_not_found").replace("%name%", internalName));
			return;
		}

		Stat stat;
		try {
			stat = Stat.valueOf(args[3].toUpperCase());
		} catch (IllegalArgumentException e) {
			sender.sendMessage(plugin.getLanguage().getMessage("item_stat_invalid_stat"));
			return;
		}

		String action = args[4].toLowerCase();

		// Eliminar/remover una stat no requiere un sexto parámetro
		if (action.equals("remove") || action.equals("delete")) {
			plugin.getItemDataManager().removeStatBonusFromItem(item.getId(), stat);
			sender.sendMessage(plugin.getLanguage().getMessage("item_stat_success").replace("%name%", internalName));
			return;
		}

		// Agregar/modificar requieren sexto parámetro, el valor
		if (args.length < 6) {
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		// Parsing para el bonusID con Espacios/Colores [OPCIONAL]
		String fullArgsString = String.join(" ", Arrays.copyOfRange(args, 5, args.length));
		String bonusID = item.getInternalName() + "_" + stat.name(); // ID por defecto
		String valueStr = args[5];

		if (fullArgsString.contains("\"")) {
			String[] parts = fullArgsString.split("\"");
			if (parts.length > 1) {
				bonusID = parts[1].replace("&", "§"); // Reemplazar & por § para colores
				// El valor numérico debería estar antes de las comillas
				valueStr = parts[0].trim();
			}
		}

		ModifierType modifier;
		try {
			modifier = ModifierType.valueOf(action.toUpperCase());
		} catch (IllegalArgumentException e) {
			sender.sendMessage(plugin.getLanguage().getMessage("item_stat_invalid_modifier"));
			return;
		}

		double value;
		try {
			value = Double.parseDouble(valueStr);
			if (value == 0) {
				sender.sendMessage(plugin.getLanguage().getMessage("item_stat_invalid_value"));
				return;
			}
		} catch (NumberFormatException e) {
			sender.sendMessage(plugin.getLanguage().getMessage("item_stat_invalid_value"));
			return;
		}

		StatBonus newBonus = new StatBonus();
		newBonus.setStatType(stat);
		newBonus.setModifierType(modifier);
		newBonus.setValue(value);
		newBonus.setBonusID(bonusID);

		plugin.getItemDataManager().addOrUpdateStatBonus(item.getId(), newBonus);

		sender.sendMessage(plugin.getLanguage().getMessage("item_stat_success").replace("%name%", internalName));
	}
}
