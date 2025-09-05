package com.shokkoh.dbsparking.datafetcher;

public class AutoLoginData {
	public final String ip;
	public final boolean kickOnMismatch;

	public AutoLoginData(String ip, boolean kickOnMismatch) {
		this.ip = ip;
		this.kickOnMismatch = kickOnMismatch;
	}
}
