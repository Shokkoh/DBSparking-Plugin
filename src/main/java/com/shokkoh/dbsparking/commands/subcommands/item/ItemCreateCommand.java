package com.shokkoh.dbsparking.commands.subcommands.item;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.items.StatsItem;
import com.shokkoh.dbsparking.items.ItemType;
import com.shokkoh.dbsparking.items.Rarity;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ItemCreateCommand extends SubCommand {

	@Override
	public String getName() {
		return "create";
	}

	@Override
	public String getDescription() {
		return "Create a new custom item based on the item in your hand.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp item create <internalName> <type> \"<display_name>\" [rarity]";
	}

	@Override public Permissions getPermission() { return Permissions.ITEM_CREATE; }

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cYou can't use this command from the console.");
			return;
		}
		// La sintaxis es: /dbsp item create <internalName> <type> "<display name>"
		if (args.length < 5) {
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		Player player = (Player) sender;
		ItemStack itemInHand = player.getItemInHand();

		if (itemInHand == null || itemInHand.getType() == Material.AIR) {
			sender.sendMessage(plugin.getLanguage().getMessage("item_create_no_item"));
			return;
		}

		String internalName = args[2];
		ItemType type;
		try {
			type = ItemType.valueOf(args[3].toUpperCase());
		} catch (IllegalArgumentException e) {
			sender.sendMessage(plugin.getLanguage().getMessage("item_create_invalid_type"));
			return;
		}

		String fullArgsString = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
		String displayName = "";
		Rarity rarity = Rarity.COMMON; // Rarity por defecto

		if (fullArgsString.contains("\"")) {
			String[] parts = fullArgsString.split("\"");
			displayName = parts[1];
			if (parts.length > 2) {
				// Si hay texto después de las comillas, es la rareza
				String rarityString = parts[2].trim();
				if (!rarityString.isEmpty()) {
					try {
						rarity = Rarity.valueOf(rarityString.toUpperCase());
					} catch (IllegalArgumentException e) {
						sender.sendMessage(plugin.getLanguage().getMessage("item_create_invalid_rarity"));
						return;
					}
				}
			}
		} else {
			sender.sendMessage("§cThe display name must be enclosed in quotes (\").");
			return;
		}

		if (plugin.getItemDataManager().itemExists(internalName)) {
			sender.sendMessage(plugin.getLanguage().getMessage("item_create_already_exists").replace("%name%", internalName));
			return;
		}

		StatsItem newItem = new StatsItem();
		newItem.setInternalName(internalName);
		newItem.setItemType(type);
		newItem.setDisplayName(displayName);
		newItem.setMaterial(itemInHand.getType());
		newItem.setItemData(itemInHand.getDurability());
		newItem.setRarity(rarity);

		int newId = plugin.getItemDataManager().createCustomItem(newItem);

		if (newId != -1) {
			sender.sendMessage(plugin.getLanguage().getMessage("item_create_success")
					.replace("%name%", internalName)
					.replace("%id%", String.valueOf(newId)));
		} else {
			sender.sendMessage("§cError saving the item to the database. Please contact an administrator.");
		}
	}
}