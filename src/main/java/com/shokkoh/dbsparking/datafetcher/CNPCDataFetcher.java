package com.shokkoh.dbsparking.datafetcher;

import org.bukkit.Bukkit;

import java.io.File;

public class CNPCDataFetcher extends AbstractDataFetcher {
	@Override
	public String getDirectory() {
		return Bukkit.getServer().getWorldContainer().getPath() + File.separator + "customnpcs" + File.separator + "playerdata";
	}

	@Override
	public String getFileExtension() {
		return ".json";
	}
}
