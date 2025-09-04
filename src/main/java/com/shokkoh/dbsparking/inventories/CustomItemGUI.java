package com.shokkoh.dbsparking.inventories;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.items.CustomItem;
import com.shokkoh.dbsparking.managers.ItemConfigManager;
import com.shokkoh.dbsparking.utils.ItemBuilder;
import com.shokkoh.dbsparking.utils.NBTUtil;
import com.shokkoh.dbsparking.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CustomItemGUI implements InventoryHolder {

	private final DBSparking plugin = DBSparking.getInstance();
	private final ItemConfigManager itemManager = plugin.getItemConfigManager();
	private final Player player;
	private final int page;
	private final Inventory inv;

	public CustomItemGUI(Player player, int page) {
		this.player = player;
		this.page = page;
		String title = plugin.getLanguage().getRawMessage("customitem.gui_title").replace("%page%", String.valueOf(page + 1));
		this.inv = Bukkit.createInventory(this, 54, title);
		setupItems();
	}

	private void setupItems() {
		ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
		ItemMeta paneMeta = pane.getItemMeta();
		paneMeta.setDisplayName(" ");
		pane.setItemMeta(paneMeta);
		for (int i = 0; i < 9; i++) inv.setItem(i, pane);
		for (int i = 45; i < 54; i++) inv.setItem(i, pane);
		for (int i = 0; i < 5; i++) {
			inv.setItem(9 + i * 9, pane);
			inv.setItem(17 + i * 9, pane);
		}

		List<CustomItem> allItems = itemManager.getAllItems();
		final int itemsPerPage = 28;
		int startIndex = page * itemsPerPage;
		int maxPages = (allItems.isEmpty()) ? 1 : (int) Math.ceil((double) allItems.size() / itemsPerPage);

		for (int i = 0; i < itemsPerPage; i++) {
			int itemIndex = startIndex + i;
			if (itemIndex >= allItems.size()) break;

			int slot = 10 + (i / 7) * 9 + (i % 7);
			CustomItem customItem = allItems.get(itemIndex);

			ItemStack displayItem = ItemBuilder.buildItemStack(customItem);
			ItemMeta meta = displayItem.getItemMeta();
			meta.setDisplayName(Text.color(customItem.getItemName()));

			List<String> lore = new ArrayList<>();
			if (customItem.getLore() != null) {
				lore.addAll(customItem.getLore().stream().map(Text::color).collect(Collectors.toList()));
			}
			lore.add(" ");
			lore.add(Text.color("&8&oInternal Name: &7" + customItem.getInternalName()));
			meta.setLore(lore);
			displayItem.setItemMeta(meta);

			inv.setItem(slot, NBTUtil.setCustomItemTag(displayItem, customItem.getInternalName()));
		}

		ItemStack createItem = new ItemStack(Material.ANVIL);
		ItemMeta createMeta = createItem.getItemMeta();
		createMeta.setDisplayName(Text.color(plugin.getLanguage().getRawMessage("customitem.button_create_name")));
		createMeta.setLore(Arrays.stream(plugin.getLanguage().getRawMessage("customitem.button_create_lore").split("\\|")).map(Text::color).collect(Collectors.toList()));
		createItem.setItemMeta(createMeta);
		inv.setItem(49, createItem);

		if (page > 0) {
			ItemStack prevPage = new ItemStack(Material.ARROW);
			ItemMeta prevMeta = prevPage.getItemMeta();
			prevMeta.setDisplayName(Text.color(plugin.getLanguage().getRawMessage("customitem.button_prev_page")));
			prevPage.setItemMeta(prevMeta);
			inv.setItem(48, prevPage);
		}

		if (page < maxPages - 1) {
			ItemStack nextPage = new ItemStack(Material.ARROW);
			ItemMeta nextMeta = nextPage.getItemMeta();
			nextMeta.setDisplayName(Text.color(plugin.getLanguage().getRawMessage("customitem.button_next_page")));
			nextPage.setItemMeta(nextMeta);
			inv.setItem(50, nextPage);
		}
	}

	public void handleClick(InventoryClickEvent event) {
		if (!event.getInventory().getHolder().equals(this)) return;
		event.setCancelled(true);

		if (event.isLeftClick() && event.getRawSlot() >= 10 && event.getRawSlot() <= 43) {
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
				player.getInventory().addItem(event.getCurrentItem().clone());
				player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1f, 1f);
			}
			return;
		}

		ItemStack clicked = event.getCurrentItem();
		if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

		String displayName = clicked.getItemMeta().getDisplayName();

		if (displayName.equals(Text.color(plugin.getLanguage().getRawMessage("customitem.button_next_page")))) {
			new CustomItemGUI(player, page + 1).open();
		} else if (displayName.equals(Text.color(plugin.getLanguage().getRawMessage("customitem.button_prev_page")))) {
			new CustomItemGUI(player, page - 1).open();
		} else if (displayName.equals(Text.color(plugin.getLanguage().getRawMessage("customitem.button_create_name")))) {
			ItemStack inHand = player.getItemInHand();
			if (inHand == null || inHand.getType() == Material.AIR) {
				player.sendMessage(plugin.getLanguage().getMessage("customitem.error_no_item"));
				player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 1f);
				player.closeInventory();
				return;
			}
			itemManager.startItemCreation(player);
			player.closeInventory();
			player.sendMessage(plugin.getLanguage().getMessage("customitem.prompt_enter_name"));
		}
	}

	public void open() {
		player.openInventory(inv);
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}
}