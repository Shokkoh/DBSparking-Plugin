package com.shokkoh.dbsparking.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;

import net.minecraft.server.v1_7_R4.NBTTagCompound;

public class NBTUtil {

	private static final String NBT_TAG_KEY = "DBSparking_InternalName";

	public static ItemStack setCustomItemTag(ItemStack bukkitItem, String internalName) {
		if (bukkitItem == null) return null;

		net.minecraft.server.v1_7_R4.ItemStack nmsItem = CraftItemStack.asNMSCopy(bukkitItem);

		NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

		compound.setString(NBT_TAG_KEY, internalName);
		nmsItem.setTag(compound);

		return CraftItemStack.asBukkitCopy(nmsItem);
	}

	public static String getCustomItemTag(ItemStack bukkitItem) {
		if (bukkitItem == null || !bukkitItem.hasItemMeta()) {
			return null;
		}

		net.minecraft.server.v1_7_R4.ItemStack nmsItem = CraftItemStack.asNMSCopy(bukkitItem);

		if (!nmsItem.hasTag()) {
			return null;
		}
		NBTTagCompound compound = nmsItem.getTag();
		if (!compound.hasKey(NBT_TAG_KEY)) {
			return null;
		}
		return compound.getString(NBT_TAG_KEY);
	}
}