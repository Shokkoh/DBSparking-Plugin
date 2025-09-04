package com.shokkoh.dbsparking.commands.subcommands.item;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import org.bukkit.command.CommandSender;

public class ItemDeleteCommand extends SubCommand {

	@Override
	public String getName() {
		return "delete";
	}

	@Override
	public String getDescription() {
		return "Deletes a custom item permanently.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp item delete <internalName>";
	}

	@Override public Permissions getPermission() {
		return Permissions.ITEM_DELETE;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (args.length < 3) {
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		String internalName = args[2];

		boolean success = plugin.getItemDataManager().deleteItemByName(internalName);

		if (success) {
			sender.sendMessage(plugin.getLanguage().getMessage("item_delete_success").replace("%name%", internalName));
		} else {
			sender.sendMessage(plugin.getLanguage().getMessage("item_not_found").replace("%name%", internalName));
		}
	}
}