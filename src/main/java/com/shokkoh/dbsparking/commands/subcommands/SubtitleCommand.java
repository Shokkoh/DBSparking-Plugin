package com.shokkoh.dbsparking.commands.subcommands;

import com.shokkoh.dbsparking.Permissions;
import com.shokkoh.dbsparking.commands.SubCommand;
import com.shokkoh.dbsparking.utils.TitleReflection;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubtitleCommand extends SubCommand {

	@Override
	public String getName() {
		return "subtitle";
	}

	@Override
	public String getDescription() {
		return "Shows a player only a subtitle.";
	}

	@Override
	public String getSyntax() {
		return "/dbsp subtitle \"<subtitle>\" <fadeIn> <stay> <fadeOut> [player]";
	}

	@Override
	public Permissions getPermission() {
		return Permissions.TITLE_SEND;
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		ParseResult subtitleResult = parseQuotedString(args, 1);
		String subtitle = subtitleResult.text;
		int nextArg = subtitleResult.nextArgIndex;

		if (args.length < nextArg + 3) {
			sender.sendMessage(plugin.getLanguage().getMessage("insufficient_arguments").replace("%syntax%", getSyntax()));
			return;
		}

		try {
			int fadeIn = Integer.parseInt(args[nextArg]) * 10;
			int stay = Integer.parseInt(args[nextArg + 1]) * 20;
			int fadeOut = Integer.parseInt(args[nextArg + 2]) * 10;

			Player target = null;
			if (args.length > nextArg + 3) {
				target = Bukkit.getPlayer(args[nextArg + 3]);
				if (target == null || !target.isOnline()) {
					sender.sendMessage(plugin.getLanguage().getMessage("player_not_online").replace("%player%", args[nextArg + 3]));
					return;
				}
			}

			if (target != null) {
				TitleReflection.sendTitle(target, null, subtitle, fadeIn, stay, fadeOut);
			} else {
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					TitleReflection.sendTitle(p, null, subtitle, fadeIn, stay, fadeOut);
				}
			}

		} catch (NumberFormatException e) {
			sender.sendMessage(plugin.getLanguage().getMessage("invalid_number"));
		}
	}

	public static class ParseResult {
		public final String text;
		public final int nextArgIndex;
		public ParseResult(String text, int nextArgIndex) { this.text = text; this.nextArgIndex = nextArgIndex; }
	}

	private static ParseResult parseQuotedString(String[] args, int startIndex) {
		if (startIndex >= args.length) return new ParseResult("", startIndex);
		String firstArg = args[startIndex];
		if (!firstArg.startsWith("\"")) return new ParseResult(firstArg, startIndex + 1);

		StringBuilder sb = new StringBuilder();
		int lastIndex = startIndex;
		for (int i = startIndex; i < args.length; i++) {
			String currentArg = args[i];
			sb.append(currentArg).append(" ");
			lastIndex = i;
			if (currentArg.endsWith("\"")) break;
		}
		String resultText = sb.toString().trim();
		if (resultText.length() > 1) {
			resultText = resultText.substring(1, resultText.length() - 1);
		} else {
			resultText = "";
		}
		return new ParseResult(resultText, lastIndex + 1);
	}
}
