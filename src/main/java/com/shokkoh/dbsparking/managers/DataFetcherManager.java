package com.shokkoh.dbsparking.managers;

import com.shokkoh.dbsparking.datafetcher.AbstractDataFetcher;
import com.shokkoh.dbsparking.datafetcher.CNPCDataFetcher;
import com.shokkoh.dbsparking.datafetcher.DBCDataFetcher;

import java.util.UUID;

public class DataFetcherManager {

	private final DBCDataFetcher dbcFetcher = new DBCDataFetcher();
	private final CNPCDataFetcher cnpcFetcher = new CNPCDataFetcher();

	/**
	 * Elimina los datos de un jugador usando el extractor apropiado.
	 * @param type El tipo de datos a borrar ("dbc" o "npc").
	 * @param playerUUID El UUID del jugador.
	 * @return true si los datos se eliminaron con éxito.
	 */
	public boolean deletePlayerData(String type, UUID playerUUID) {
		AbstractDataFetcher fetcher;
		switch (type.toLowerCase()) {
			case "dbc":
				fetcher = dbcFetcher;
				break;
			case "cnpc":
			case "npc":
				fetcher = cnpcFetcher;
				break;
			case "all" :
				boolean dbcDeleted = dbcFetcher.tryToDeleteData(playerUUID);
				boolean cnpcDeleted = cnpcFetcher.tryToDeleteData(playerUUID);
				return dbcDeleted && cnpcDeleted;
			default:
				return false;
		}
		return fetcher.tryToDeleteData(playerUUID);
	}
}
