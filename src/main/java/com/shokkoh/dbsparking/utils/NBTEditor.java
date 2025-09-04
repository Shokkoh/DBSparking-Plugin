package com.shokkoh.dbsparking.utils;

import net.minecraft.server.v1_7_R4.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NBTEditor {

	private static final String PLUGIN_TAG_ID = "DBSparking_Item";

	/**
	 * Añade una etiqueta NBT a un ItemStack para identificarlo como un ítem personalizado.
	 * @param item El ItemStack a etiquetar.
	 * @param internalName El nombre interno del ítem (ej. "PecheraVIP").
	 * @return El ItemStack con la etiqueta NBT añadida.
	 */
	public static ItemStack setItemTag(ItemStack item, String internalName) {
		net.minecraft.server.v1_7_R4.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		NBTTagCompound compound = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();

		compound.setString(PLUGIN_TAG_ID, internalName);
		nmsItem.setTag(compound);

		return CraftItemStack.asBukkitCopy(nmsItem);
	}

	/**
	 * Lee la etiqueta NBT de un ItemStack para obtener su nombre interno.
	 * @param item El ItemStack a revisar.
	 * @return El nombre interno del ítem, o null si no es un ítem del plugin.
	 */
	public static String getItemTag(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) {
			return null;
		}
		net.minecraft.server.v1_7_R4.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		if (!nmsItem.hasTag()) {
			return null;
		}
		NBTTagCompound compound = nmsItem.getTag();
		if (!compound.hasKey(PLUGIN_TAG_ID)) {
			return null;
		}
		return compound.getString(PLUGIN_TAG_ID);
	}
}