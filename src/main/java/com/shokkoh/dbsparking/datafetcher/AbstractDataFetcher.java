package com.shokkoh.dbsparking.datafetcher;

import java.io.File;
import java.util.UUID;

public abstract class AbstractDataFetcher {

	/**
	 * Intenta eliminar el archivo de datos para un jugador específico.
	 * @param playerUUID El UUID del jugador cuyos datos se eliminarán.
	 * @return true si el archivo fue encontrado y eliminado, false en caso contrario.
	 */
	public boolean tryToDeleteData(UUID playerUUID) {
		File dataFile = new File(getDirectory(), playerUUID.toString() + getFileExtension());
		if (dataFile.exists()) {
			return dataFile.delete();
		}
		return false;
	}

	/**
	 * Devuelve la ruta a la carpeta donde se guardan los datos del mod.
	 */
	public abstract String getDirectory();

	/**
	 * Devuelve la extensión del archivo de datos (ej. ".dat").
	 */
	public abstract String getFileExtension();
}