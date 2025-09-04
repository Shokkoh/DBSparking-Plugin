package com.shokkoh.dbsparking.utils;

import com.shokkoh.dbsparking.items.CustomItem;
import net.minecraft.server.v1_7_R4.*;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ItemBuilder {

	public static ItemStack buildItemStack(CustomItem customItem) {
		if (customItem == null) return null;

		ItemStack bukkitItem = new ItemStack(customItem.getItemId(), 1, (short) customItem.getItemData(), (byte) customItem.getItemData());

		ItemMeta meta = bukkitItem.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(Text.color(customItem.getItemName()));

			List<String> finalLore = new ArrayList<>();
			if (customItem.getLore() != null) {
				finalLore.addAll(customItem.getLore().stream().map(Text::color).collect(Collectors.toList()));
			}
			meta.setLore(finalLore);
			bukkitItem.setItemMeta(meta);
		}

		bukkitItem = NBTUtil.setCustomItemTag(bukkitItem, customItem.getInternalName());

		if (customItem.getDamage() >= 0) {
			bukkitItem = applyAttackDamageAttribute(bukkitItem, customItem.getDamage());
		}

		return bukkitItem;
	}

	private static ItemStack applyAttackDamageAttribute(ItemStack bukkitItem, double damage) {
		net.minecraft.server.v1_7_R4.ItemStack nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
		NBTTagCompound tag = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

		NBTTagList modifiers = tag.hasKey("AttributeModifiers") ? tag.getList("AttributeModifiers", 10) : new NBTTagList();

		NBTTagList newModifiers = new NBTTagList();
		for (int i = 0; i < modifiers.size(); i++) {
			NBTTagCompound modifier = modifiers.get(i);
			if (modifier != null && !modifier.getString("Name").equals("dbsparking_damage")) {
				newModifiers.add(modifier);
			}
		}

		NBTTagCompound damageModifier = new NBTTagCompound();
		UUID modifierUUID = UUID.randomUUID();

		damageModifier.setString("AttributeName", "generic.attackDamage");
		damageModifier.setString("Name", "dbsparking_damage");
		damageModifier.setDouble("Amount", damage);
		damageModifier.setInt("Operation", 0);
		damageModifier.setLong("UUIDMost", modifierUUID.getMostSignificantBits());
		damageModifier.setLong("UUIDLeast", modifierUUID.getLeastSignificantBits());

		newModifiers.add(damageModifier);

		tag.set("AttributeModifiers", newModifiers);
		nmsItem.setTag(tag);

		return CraftItemStack.asBukkitCopy(nmsItem);
	}
}