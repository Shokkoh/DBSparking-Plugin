package com.shokkoh.dbsparking.commands.subcommands;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AutoLoginCommand extends SubCommand {

	private final Map<UUID, Long> pendingConfirmations = new HashMap<>();

	@Override
	public String getName() {
		return "autologin";
	}

	@Override
	public String getDescription() {
		return "Links your current IP to your account for automatic login.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp autologin <password>";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.PLAYER;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.getLanguage().getMessage("only_players"));
			return;
		}

		Player player = (Player) sender;

		if (args.length < 2) {
			sender.sendMessage(plugin.getLanguage().getMessage("autologin_password_required"));
			return;
		}
		String password = args[1];

		AuthMeApi authMeApi = AuthMeApi.getInstance();
		if (!authMeApi.checkPassword(player.getName(), password)) {
			player.sendMessage(plugin.getLanguage().getMessage("autologin_incorrect_password"));
			pendingConfirmations.remove(player.getUniqueId());
			return;
		}

		UUID playerUUID = player.getUniqueId();
		if (pendingConfirmations.containsKey(playerUUID)) {
			long timeSinceRequest = System.currentTimeMillis() - pendingConfirmations.get(playerUUID);
			if (timeSinceRequest <= 10000) {
				plugin.getAutoLoginManager().setAutoLogin(player);
				player.sendMessage(plugin.getLanguage().getMessage("autologin_success"));
				pendingConfirmations.remove(playerUUID);
			} else {
				player.sendMessage(plugin.getLanguage().getMessage("autologin_expired"));
				pendingConfirmations.remove(playerUUID);
			}
		} else {
			pendingConfirmations.put(playerUUID, System.currentTimeMillis());
			String playerIP = player.getAddress().getAddress().getHostAddress();

			List<String> confirmationLines = plugin.getLanguage().getStringList("autologin_confirm");

			for (String line : confirmationLines) {
				String formattedLine = line
						.replace("%ip%", playerIP)
						.replace("%nick%", player.getName());
				player.sendMessage(formattedLine);
			}
		}
	}
}