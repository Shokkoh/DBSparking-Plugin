package com.shokkoh.dbsparking.datafetcher;

import org.bukkit.Bukkit;

import java.io.File;

public class DBCDataFetcher extends AbstractDataFetcher {
	@Override
	public String getDirectory() {
		return Bukkit.getServer().getWorldContainer().getPath() + File.separator + "playerdata";
	}

	@Override
	public String getFileExtension() {
		return ".dat";
	}
}
