package com.shokkoh.dbsparking.commands.subcommands.item;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.items.StatsItem;
import com.shokkoh.dbsparking.utils.NBTEditor;
import com.shokkoh.dbsparking.utils.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemGiveCommand extends SubCommand {

	@Override
	public String getName() {
		return "give";
	}

	@Override
	public String getDescription() {
		return "Give a custom item to a player.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp item give <internalName> [player]";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.ITEM_GIVE;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (args.length < 3) {
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		Player player = (Player) sender;
		String internalName = args[2];

		StatsItem statsItem = plugin.getItemDataManager().getItemByName(internalName);
		if (statsItem == null) {
			player.sendMessage(plugin.getLanguage().getMessage("item_not_found").replace("%name%", internalName));
			return;
		}

		ItemStack itemStack = new ItemStack(statsItem.getMaterial(), 1, (short) statsItem.getItemData());
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Text.color(statsItem.getDisplayName()));

		List<String> finalLore = new ArrayList<>();
		// 1. Añadir el lore personalizado primero
		if (statsItem.getCustomLore() != null && !statsItem.getCustomLore().isEmpty()) {
			for (String line : statsItem.getCustomLore()) {
				finalLore.add(Text.color(line));
			}
		}
		// 2. Añadir el lore de las stats
		List<String> statLore = plugin.getItemDataManager().getStatLoreFor(statsItem);
		if (!statLore.isEmpty()) {
			if (!finalLore.isEmpty()) {
				finalLore.add("");
			}
			finalLore.addAll(statLore);
		}

		// 3. Añadir el lore final personalizado
		if (statsItem.getFinalLore() != null && !statsItem.getFinalLore().isEmpty()) {
			if (!finalLore.isEmpty()) {
				finalLore.add(""); // Espaciador
				finalLore.add(plugin.getLanguage().getRawMessage("item_final_lore_header"));
			}
			for (String line : statsItem.getFinalLore()) {
				finalLore.add(Text.color(line));
			}
		}

		// 4. Añadir la rareza al final
		if (!finalLore.isEmpty()) {
			finalLore.add("");
		}
		String rarityPath = "item_rarities." + statsItem.getRarity().name().toLowerCase();
		finalLore.add(plugin.getLanguage().getRawMessage(rarityPath));

		meta.setLore(finalLore);
		itemStack.setItemMeta(meta);

		// Etiquetamos el ítem con su nombre interno
		itemStack = NBTEditor.setItemTag(itemStack, statsItem.getInternalName());

		player.getInventory().addItem(itemStack);
		String coloredDisplayName = Text.color(statsItem.getDisplayName());
		sender.sendMessage(plugin.getLanguage().getMessage("item_give_success")
				.replace("%name%", coloredDisplayName)
				.replace("%player%", player.getName()));
	}
}