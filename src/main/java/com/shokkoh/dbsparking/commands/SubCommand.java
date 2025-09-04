package com.shokkoh.dbsparking.commands;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.Permissions;
import org.bukkit.command.CommandSender;

public abstract class SubCommand {

	protected final DBSparking plugin = DBSparking.getInstance();

	/**
	 * El nombre del subcomando (ej. "reload").
	 */
	public abstract String getName();

	/**
	 * La descripción del subcomando para el menú de ayuda.
	 */
	public abstract String getDescription();

	/**
	 * La sintaxis del subcomando (ej. "/dbs reload").
	 */
	public abstract String getSyntax();

	/**
	 * El permiso necesario para ejecutar este subcomando.
	 */
	public abstract Permissions getPermission();

	/**
	 * El código que se ejecuta cuando se llama al subcomando.
	 * @param sender Quien ejecuta el comando.
	 * @param args Los argumentos que siguen al nombre del subcomando.
	 */
	public abstract void perform(CommandSender sender, String[] args);
}