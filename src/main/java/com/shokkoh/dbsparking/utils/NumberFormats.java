package com.shokkoh.dbsparking.utils;

import com.shokkoh.dbsparking.DBSparking;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.TimeUnit;

public final class NumberFormats {
	private NumberFormats(){}

	public static String formatInt(long value) {
		String pattern = DBSparking.getInstance().getConfig().getString("number_format.pattern", "#,###");
		String sepStr  = DBSparking.getInstance().getConfig().getString("number_format.grouping_separator", ".");
		char sep = (sepStr != null && !sepStr.isEmpty()) ? sepStr.charAt(0) : '.';

		DecimalFormatSymbols sym = new DecimalFormatSymbols();
		sym.setGroupingSeparator(sep);
		DecimalFormat df = new DecimalFormat(pattern, sym);
		df.setGroupingUsed(true);
		return df.format(value);
	}

	public static String formatDuration(long totalSeconds) {
		if (totalSeconds <= 0) return "Expired";
		long hours = TimeUnit.SECONDS.toHours(totalSeconds);
		long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60;
		long seconds = totalSeconds % 60;

		if (hours >= 1) {
			return hours + "h";
		} else if (minutes >= 1) {
			return minutes + "m " + seconds + "s";
		} else {
			return seconds + "s";
		}
	}
}

