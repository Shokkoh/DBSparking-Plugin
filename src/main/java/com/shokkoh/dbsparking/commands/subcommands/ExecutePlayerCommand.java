package com.shokkoh.dbsparking.commands.subcommands;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ExecutePlayerCommand extends SubCommand {

	@Override
	public String getName() {
		return "eap";
	}

	@Override
	public String getDescription() {
		return "Execute a command or send a chat message as another player.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp eap <player> <command/message>";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.ADMIN;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {

		if (args.length < 3) {
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		Player targetPlayer = Bukkit.getPlayer(args[1]);
		if (targetPlayer == null) {
			sender.sendMessage(plugin.getLanguage().getMessage("commands.player_not_found").replace("%player%", args[1]));
			return;
		}

		StringBuilder inputBuilder = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			inputBuilder.append(args[i]).append(" ");
		}
		String input = inputBuilder.toString().trim();

		if (input.isEmpty()) {
			sender.sendMessage(plugin.getLanguage().getMessage("eap_input_missing"));
			return;
		}

		if (input.startsWith("/")) {
			String commandToExecute = input.substring(1);
			boolean wasOp = targetPlayer.isOp();

			try {
				targetPlayer.setOp(true);
				targetPlayer.performCommand(commandToExecute);

				String successMessage = plugin.getLanguage().getMessage("eap_command_success")
						.replace("%player%", targetPlayer.getName())
						.replace("%input%", input);
				sender.sendMessage(successMessage);

			} catch (Exception e) {
				String errorMessage = plugin.getLanguage().getMessage("eap_command_error")
						.replace("%error%", e.getMessage());
				sender.sendMessage(errorMessage);

			} finally {
				targetPlayer.setOp(wasOp);
			}
		} else {
			targetPlayer.chat(input);

			String successMessage = plugin.getLanguage().getMessage("eap_chat_success")
					.replace("%player%", targetPlayer.getName())
					.replace("%input%", input);
			sender.sendMessage(successMessage);
		}
	}
}