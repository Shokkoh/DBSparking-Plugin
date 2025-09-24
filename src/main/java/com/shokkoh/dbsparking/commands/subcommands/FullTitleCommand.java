package com.shokkoh.dbsparking.commands.subcommands;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.utils.TitleReflection;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FullTitleCommand extends SubCommand implements Listener {

	private final Map<UUID, TitleInput> awaitingInput = new HashMap<>();

	@Override
	public String getName() {
		return "fulltitle";
	}

	@Override
	public String getDescription() {
		return "Shows a player a full title and subtitle.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp fulltitle <fadeIn> <stay> <fadeOut> [player]";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.TITLE_SEND;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can run this command.");
			return;
		}

		Player player = (Player) sender;

		if (args.length < 4) {
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		try {
			int fadeIn = Integer.parseInt(args[1]);
			int stay = Integer.parseInt(args[2]);
			int fadeOut = Integer.parseInt(args[3]);

			Player target = null;
			if (args.length > 4) {
				target = Bukkit.getPlayer(args[4]);
				if (target == null || !target.isOnline()) {
					sender.sendMessage(plugin.getLanguage().getMessage("player_not_online").replace("%player%", args[3]));
					return;
				}
			}

			// Guardamos la info de la sesión del input
			awaitingInput.put(player.getUniqueId(), new TitleInput(player, target, fadeIn, stay, fadeOut, 0, "", ""));

			player.sendMessage(plugin.getLanguage().getMessage("write_title"));

			// Registramos el listener si no está registrado
			Bukkit.getPluginManager().registerEvents(this, plugin);

		} catch (NumberFormatException e) {
			sender.sendMessage(plugin.getLanguage().getMessage("invalid_number"));
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (!awaitingInput.containsKey(player.getUniqueId())) return;

		event.setCancelled(true); // Cancelamos para que no se muestre en chat
		TitleInput input = awaitingInput.get(player.getUniqueId());
		if (event.getMessage().equals("cancel")) {
			player.sendMessage(plugin.getLanguage().getMessage("title_cancelled"));
			awaitingInput.remove(player.getUniqueId());
			return;
		}

		if (input.step == 0) {
			input.title = event.getMessage();
			input.step = 1;
			player.sendMessage(plugin.getLanguage().getMessage("write_subtitle"));
		} else if (input.step == 1) {
			input.subtitle = event.getMessage();

			// Enviamos el título
			if (input.target != null) {
				TitleReflection.sendTitle(input.target, input.title, input.subtitle, input.fadeIn, input.stay, input.fadeOut);
			} else {
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					TitleReflection.sendTitle(p, input.title, input.subtitle, input.fadeIn, input.stay, input.fadeOut);
				}
			}

			player.sendMessage(plugin.getLanguage().getMessage("title_sent"));
			awaitingInput.remove(player.getUniqueId());
		}
	}

	private static class TitleInput {
		Player sender;
		Player target;
		int fadeIn, stay, fadeOut;
		int step;
		String title;
		String subtitle;

		public TitleInput(Player sender, Player target, int fadeIn, int stay, int fadeOut, int step, String title, String subtitle) {
			this.sender = sender;
			this.target = target;
			this.fadeIn = fadeIn;
			this.stay = stay;
			this.fadeOut = fadeOut;
			this.step = step;
			this.title = title;
			this.subtitle = subtitle;
		}
	}
}
