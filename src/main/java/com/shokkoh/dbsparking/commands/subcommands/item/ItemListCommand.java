package com.shokkoh.dbsparking.commands.subcommands.item;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.items.StatsItem;
import com.shokkoh.dbsparking.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ItemListCommand extends SubCommand {
	private static final int ITEMS_PER_PAGE = 10;

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public String getDescription() {
		return "Lists all custom items.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp item list [page]";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.ITEM_LIST;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cThis command can only be used by players.");
			return;
		}
		Player player = (Player) sender;
		List<StatsItem> allItems = plugin.getItemDataManager().getAllItems();

		if (allItems.isEmpty()) {
			sender.sendMessage(plugin.getLanguage().getMessage("item_list_empty"));
			return;
		}

		// --- 1. Parseo y Cálculo de Páginas ---
		int page = 1;
		if (args.length >= 3) {
			try {
				page = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				sender.sendMessage("§c'" + args[2] + "' is not a valid page number.");
				return;
			}
		}

		int totalPages = (int) Math.ceil((double) allItems.size() / ITEMS_PER_PAGE);
		if (totalPages == 0) totalPages = 1; // Asegurar que siempre haya al menos 1 página
		if (page < 1 || page > totalPages) {
			sender.sendMessage("§cInvalid page number. Please choose a page between 1 and " + totalPages + ".");
			return;
		}

		// Ordenar la lista alfabéticamente
		Collections.sort(allItems, new Comparator<StatsItem>() {
			@Override
			public int compare(StatsItem item1, StatsItem item2) {
				return item1.getInternalName().compareToIgnoreCase(item2.getInternalName());
			}
		});

		// --- 2. Mostrar Header y los Ítems de la Página Actual ---
		player.sendMessage(Text.color(String.format("&e< &6-=-=-=-=- &bCustom Items (Page %d/%d) &6-=-=-=-=- &e>", page, totalPages)));

		int startIndex = (page - 1) * ITEMS_PER_PAGE;
		int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());

		for (int i = startIndex; i < endIndex; i++) {
			StatsItem item = allItems.get(i);

			String coloredDisplayName = Text.color(item.getDisplayName());
			String safeDisplayName = coloredDisplayName.replace("\"", "\\\""); // Escapar comillas

			// Texto normal
			String display = String.format("{\"text\":\"§6%d. §f%s §7| §e%s §7| %s §7| \"}",
					(i + 1),
					item.getInternalName(),
					item.getItemType().name(),
					safeDisplayName
			);

			// Texto clickeable [Stats]
			String stats = String.format("{\"text\":\"§8[§eStats§8]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/dbsp item info %s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§aClick!\"}}",
					item.getInternalName()
			);

			String separator = "{\"text\":\" §7| \"}";

			// Texto clickeable [Give]
			String give = String.format("{\"text\":\" §8[§aGive§8]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/dbsp item give %s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§aClick!\"}}",
					item.getInternalName()
			);

			String tellrawCommand = "tellraw " + player.getName() + " [\"\"," + display + "," + stats + "," + separator + "," + give + "]";
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), tellrawCommand);
		}

		// --- 3. Construcción del Footer Interactivo ---
		if (totalPages > 1) {
			String headerFooterPart = "{\"text\":\"" + Text.color("&e< &6-=-=-=-=- ") + "\"}";

			String prevButton;
			if (page > 1) {
				prevButton = String.format("{\"text\":\"§a≪ Prev\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/dbsp item list %d\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§eGo to page %d\"}}", (page - 1), (page - 1));
			} else {
				prevButton = "{\"text\":\"§7≪ Prev\"}";
			}

			String pageIndicator = String.format("{\"text\":\"" + Text.color(" &b(Page %d/%d) &r") + "\"}", page, totalPages);

			String nextButton;
			if (page < totalPages) {
				nextButton = String.format("{\"text\":\"§aNext ≫\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/dbsp item list %d\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§eGo to page %d\"}}", (page + 1), (page + 1));
			} else {
				nextButton = "{\"text\":\"§7Next ≫\"}";
			}

			String endFooterPart = "{\"text\":\"" + Text.color(" &6-=-=-=-=- &e>") + "\"}";

			// Unimos todos los componentes del footer
			String footerTellraw = "tellraw " + player.getName() + " [" + headerFooterPart + "," + prevButton + "," + pageIndicator + "," + nextButton + "," + endFooterPart + "]";

			// Pequeña demora para que el footer aparezca después de la lista
			final String finalCommand = footerTellraw;
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
				}
			}, 2L);
		}
	}
}
