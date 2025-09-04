package com.shokkoh.dbsparking;

import org.bukkit.command.CommandSender;

public enum Permissions {
	// Permisos de Administrador
	ADMIN("dbsparking.admin"),
	RELOAD("dbsparking.reload"),
	HELP("dbsparking.help"),
	TPS("dbsparking.tps"),
	DATA_MANAGE("dbsparking.data.manage"),
	TITLE_SEND("dbsparking.title.send"),

	// Comandos de Boosts
	BOOST_DELETE("dbsparking.boost.delete"),
	BOOST_LIST("dbsparking.boost.list"),
	BOOST_ADD("dbsparking.boost.add"),

	// Comandos de Items
	ITEM_LIST("dbsparking.item.list"),
	ITEM_GIVE("dbsparking.item.give"),
	ITEM_DELETE("dbsparking.item.delete"),
	ITEM_CREATE("dbsparking.item.add"),
	ITEM_STAT("dbsparking.item.stat"),
	ITEM_EDIT("dbsparking.item.edit"),
	ITEM_DROP("dbsparking.item.drop"),
	ITEM_CONDITION("dbsparking.item.condition"),
	ITEM_CONDITION_OVERRIDE("dbsparking.item.condition.override"),
	ITEM_LORE("dbsparking.item.lore"),
	SOUL_INVENTORY("dbsparking.soulinventory"),

	// General
	PLAYER("dbsparking.player");

	private final String permission;

	Permissions(String permission) {
		this.permission = permission;
	}

	public String getPermission() {
		return permission;
	}

	public boolean has(CommandSender sender) {
		// La consola tiene todos los permisos, un usuario con Permission.ADMIN también
		if (sender.isOp() || sender.hasPermission("dbsparking.admin") || !(sender instanceof org.bukkit.entity.Player)) {
			return true;
		}

		return sender.hasPermission(this.permission);
	}
}