package com.shokkoh.dbsparking.commands.subcommands;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.entities.DBSPlayer;
import com.shokkoh.dbsparking.utils.NumberFormats;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPSCommand extends SubCommand {
	@Override
	public String getName() {
		return "tps";
	}

	@Override
	public String getDescription() {
		return "Gives Training Points to a player with a manageable combo system.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp tps <amount> [player] [applyCombo]";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.TPS;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(plugin.getLanguage().getRawMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		int baseAmount;
		try {
			baseAmount = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			sender.sendMessage(plugin.getLanguage().getRawMessage("invalid_number"));
			return;
		}

		Player target;
		if (args.length >= 3) {
			target = Bukkit.getPlayer(args[2]);
			if (target == null || !target.isOnline()) {
				sender.sendMessage(plugin.getLanguage().getRawMessage("player_not_online").replace("%player%", args[2]));
				return;
			}
		} else {
			if (!(sender instanceof Player)) {
				sender.sendMessage("§cConsole must specify a player.");
				return;
			}
			target = (Player) sender;
		}

		boolean applyCombo = true;
		if (args.length >= 4) {
			String applyComboArg = args[3].toLowerCase();
			if (applyComboArg.equals("false") || applyComboArg.equals("no")) {
				applyCombo = false;
			} else if (!applyComboArg.equals("true") && !applyComboArg.equals("yes")) {
				sender.sendMessage(plugin.getLanguage().getRawMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
				return;
			}
		}
		if (!(plugin.getConfig().getBoolean("tps_combo_system.enabled", true))) {
			applyCombo = false;
		}

		DBSPlayer dbsPlayer = plugin.getPlayerManager().getDBSPlayer(target);
		long currentTime = System.currentTimeMillis();
		long comboDelay = plugin.getConfig().getLong("tps_combo_system.delay_seconds", 3) * 1000;

		if (dbsPlayer.getTpsToGive() > 0 && (currentTime - dbsPlayer.getComboTimestamp() < comboDelay)) {
			dbsPlayer.setCombo(dbsPlayer.getCombo() + 1); // Continuar combo
		} else {
			dbsPlayer.setCombo(1);
		}

		double comboBonusPercent = Math.min(
				plugin.getConfig().getDouble("tps_combo_system.max_bonus_percentage", 25.0),
				(dbsPlayer.getCombo() - 1) * plugin.getConfig().getDouble("tps_combo_system.bonus_per_hit", 2.0)
		);


		double comboMultiplier;
		if (applyCombo && dbsPlayer.getCombo() > 1) {
			comboMultiplier = 1.0 + (comboBonusPercent / 100.0);
		} else {
			comboMultiplier = 1.0;
		}

		double boosterMultiplier = plugin.getBoostManager().getFinalBoost(target);
		int bonusAmount = (int) Math.round(baseAmount * boosterMultiplier - baseAmount);
		int comboAmount = (int) Math.round(baseAmount * comboMultiplier - baseAmount);
		int finalAmount = baseAmount + bonusAmount + comboAmount;

		dbsPlayer.addTps(dbsPlayer.getTpsToGive() + finalAmount);
		dbsPlayer.setComboTimestamp(currentTime);

		String gaveMsg;
		String receivedMsg;
		String comboMsg;

		if (boosterMultiplier > 1.0) {
			gaveMsg = plugin.getLanguage().getMessage("tps_gave_boosted")
					.replace("%bonus_amount%", NumberFormats.formatInt(bonusAmount))
					.replace("%final_amount%", NumberFormats.formatInt(finalAmount))
					.replace("%player%", target.getName());

			receivedMsg = plugin.getLanguage().getRawMessage("tps_received_boosted")
					.replace("%final_amount%", NumberFormats.formatInt(finalAmount))
					.replace("%bonus_amount%", NumberFormats.formatInt(bonusAmount))
					.replace("%boost_percent%", String.format("%.0f", (boosterMultiplier - 1.0) * 100));
		} else {
			gaveMsg = plugin.getLanguage().getMessage("tps_gave_normal")
					.replace("%amount%", NumberFormats.formatInt(finalAmount))
					.replace("%player%", target.getName());

			receivedMsg = plugin.getLanguage().getRawMessage("tps_received_normal")
					.replace("%amount%", NumberFormats.formatInt(finalAmount));
		}

		if (applyCombo && comboMultiplier > 1.0) {
			dbsPlayer.setComboTimestamp(currentTime);

			if (comboBonusPercent < plugin.getConfig().getDouble("tps_combo_system.max_bonus_percentage", 25.0)) {
				comboMsg = plugin.getLanguage().getRawMessage("tps_combo_bonus")
						.replace("%combo%", String.valueOf(dbsPlayer.getCombo()))
						.replace("%combo_amount%", NumberFormats.formatInt(comboAmount))
						.replace("%combo_percent%", String.format("%.0f", comboBonusPercent));
			} else {
				comboMsg = plugin.getLanguage().getMessage("tps_combo_max")
						.replace("%combo_amount%", NumberFormats.formatInt(comboAmount))
						.replace("%combo_percent%", String.format("%.0f", comboBonusPercent));
			}
		} else comboMsg = "";

		if (sender != target) {
			sender.sendMessage(gaveMsg + " " + comboMsg);
		}
		target.sendMessage(receivedMsg + " " + comboMsg);
	}
}