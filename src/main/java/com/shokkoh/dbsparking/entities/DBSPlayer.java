package com.shokkoh.dbsparking.entities;

import com.shokkoh.dbsparking.DBSparking;
import com.shokkoh.dbsparking.boosters.ActiveBoost;
import com.shokkoh.dbsparking.boosters.BoostType;
import com.shokkoh.dbsparking.items.StatsItem;
import com.shokkoh.dbsparking.items.ItemType;
import com.shokkoh.dbsparking.managers.DBCManager;
import com.shokkoh.dbsparking.utils.NBTEditor;
import com.shokkoh.dbsparking.utils.NumberFormats;
import com.shokkoh.dbsparking.utils.Text;
import kamkeel.npcdbc.api.IDBCAddon;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.api.handler.data.*;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.*;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.quests.QuestInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.entity.ScriptPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Representa a un jugador dentro del plugin.
 * Contiene datos de sesión para un acceso rápido y eficiente.
 * Será utilizada para los items y demás.
 */
public class DBSPlayer {
	private final UUID uuid;
	private int level;
	private int raceId;
	private final transient Player player;
	private final transient OfflinePlayer offlinePlayer;
	private List<ActiveBoost> activeBoosts = new ArrayList<>();
	private final Map<Integer, StatsItem> equippedSouls = new HashMap<>();
	private final Map<Integer, Boolean> slotOverrides = new HashMap<>();
	private List<Integer> unlockedSoulSlots = new ArrayList<>();
	private final DBSparking plugin = DBSparking.getInstance();
	DBCManager dbcManager = plugin.getDbcManager();

	private int tpsToGive = 0;
	private int combo = 0;
	private long comboTimestamp = 0;

	public DBSPlayer(Player player) {
		this.uuid = player.getUniqueId();
		this.player = player;
		this.offlinePlayer = player;
		this.level = getLevel();
		this.raceId = getRaceId();
	}

	public DBSPlayer(OfflinePlayer offlinePlayer, int level, int raceId) {
		this.uuid = offlinePlayer.getUniqueId();
		this.player = null;
		this.offlinePlayer = offlinePlayer;
		this.level = level;
		this.raceId = raceId;
	}

	public UUID getUuid() {
		return uuid;
	}

	public Player getPlayer() {
		return player;
	}

	public List<ActiveBoost> getActiveBoosts() {
		return activeBoosts;
	}

	public void setActiveBoosts(List<ActiveBoost> activeBoosts) {
		this.activeBoosts = activeBoosts;
	}

	public int getTpsToGive() {
		return tpsToGive;
	}

	public void addTps(int tpsToGive) {
		this.tpsToGive = tpsToGive;
	}

	public int getCombo() {
		return combo;
	}

	public void setCombo(int combo) {
		this.combo = combo;
	}

	public long getComboTimestamp() {
		return comboTimestamp;
	}

	public void setComboTimestamp(long comboTimestamp) {
		this.comboTimestamp = comboTimestamp;
	}

	public void loadEquippedSouls() {
		this.equippedSouls.clear();

		Map<Integer, String> savedSouls = plugin.getItemDataManager().getEquippedSoulsForPlayer(getUuid());

		for (Map.Entry<Integer, String> entry : savedSouls.entrySet()) {
			int slot = entry.getKey();
			String internalName = entry.getValue();

			StatsItem soulItem = plugin.getItemDataManager().getItemByName(internalName);
			if (soulItem != null) {
				this.equippedSouls.put(slot, soulItem);
			}
		}
	}

	public StatsItem getEquippedSoul(int soulSlot) {
		return equippedSouls.get(soulSlot);
	}

	public StatsItem getEquippedSoul(String internalName) {
		for (StatsItem soul : equippedSouls.values()) {
			if (soul.getInternalName().equalsIgnoreCase(internalName)) {
				return soul;
			}
		}
		return null;
	}

	public void overrideSoulSlot(int soulSlot, boolean override) {
		slotOverrides.put(soulSlot, override);
	}

	public boolean canEquipSoul(int soulSlot) {
		if (slotOverrides.containsKey(soulSlot) && slotOverrides.get(soulSlot)) {
			return true;
		}

		if (soulSlot == 1) {
			return true;
		}

		if (soulSlot == 2) {
			String requiredRank = plugin.getConfig().getString("souls_inventory.slot2.required_rank", "renacer2");
			return DBSparking.perms != null && DBSparking.perms.playerInGroup(player, requiredRank) || player.hasPermission("group." + requiredRank);
		}

		if (soulSlot == 3) {
			if ((DBSparking.perms != null && DBSparking.perms.playerInGroup(player, "vip") || player.hasPermission("group.vip")) ||
					(DBSparking.perms != null && DBSparking.perms.playerInGroup(player, "vip2") || player.hasPermission("group.vip2"))) return true;
		}

		if (soulSlot == 4) {
			return DBSparking.perms != null && DBSparking.perms.playerInGroup(player, "vip2") || player.hasPermission("group.vip2");
		}
		return false;
	}

	public void setEquippedSoul(int slot, StatsItem item) {
		if (item == null) {
			equippedSouls.remove(slot);
			plugin.getItemDataManager().unequipSoul(getUuid(), slot);
		} else {
			equippedSouls.put(slot, item);
			plugin.getItemDataManager().equipSoul(getUuid(), slot, item.getInternalName());
		}
	}

	public boolean hasCustomItemInHand() {
		org.bukkit.inventory.ItemStack itemInHand = player.getItemInHand();
		if (itemInHand == null) return false;
		String internalName = NBTEditor.getItemTag(itemInHand);
		if (internalName == null) return false;
		StatsItem statsItem = plugin.getItemDataManager().getItemByName(internalName);
		return statsItem != null;
	}

	public boolean hasCustomArmorEquipped() {
		org.bukkit.inventory.ItemStack[] armorContents = player.getInventory().getArmorContents();
		for (org.bukkit.inventory.ItemStack armorPiece : armorContents) {
			if (armorPiece != null) {
				String internalName = NBTEditor.getItemTag(armorPiece);
				if (internalName != null) {
					StatsItem statsItem = plugin.getItemDataManager().getItemByName(internalName);
					if (statsItem != null && statsItem.getItemType() == ItemType.ARMOR) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isOnline() {
		return this.player != null && this.player.isOnline();
	}

	public int getLevel() {
		if (isOnline()) return this.dbcManager.getLevel(this.player);
		return this.level;
	}

	public int getRaceId() {
		if (isOnline()) return this.dbcManager.getRace(this.player);
		return this.raceId;
	}

	public String getRaceName() {
		int raceId = getRaceId();
		return plugin.getLanguage().getRawMessage("placeholder.race_" + raceId);
	}

	public int getStat(String stat) {
		return dbcManager.getStat(player, stat);
	}

	public int getClassId() {
		return dbcManager.getDBCClass(player);
	}

	public String getClassName() {
		int classId = dbcManager.getDBCClass(player);
		return plugin.getLanguage().getRawMessage("placeholder.class_" + classId);
	}

	public void giveTps(int tpsToGive) {
		dbcManager.addTP(player, tpsToGive);
	}

	public int getTps() {
		return dbcManager.getTP(player);
	}

	public String getTpsFormatted() {
		return NumberFormats.formatInt(dbcManager.getTP(player));
	}

	public void executeCommand(String command) {
		plugin.getServer().dispatchCommand(player, command.replace("%player%", player.getName()));
	}

	public void executeOPCommand(String command) {
		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("%player%", player.getName()));
	}

	public void setSkill(String skillName, int level) {
		String skill = getSkillName(skillName);
		if (skill != null) {
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "dbcskill givelvl " + skillName + " " + level + " " + player.getName());
		}
	}

	public void removeSkill(String skillName) {
		String skill = getSkillName(skillName);
		if (skill != null) {
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "dbcskill take " + skillName + " " + player.getName());
		}
	}

	public String getPrimaryGroup() {
		if (DBSparking.luckPerms != null) {
			return DBSparking.luckPerms.getUserManager().getUser(player.getUniqueId()).getPrimaryGroup();
		} else if (DBSparking.perms != null) {
			return DBSparking.perms.getPrimaryGroup(player);
		} else {
			return "default";
		}
	}

	public boolean isInGroup(String group) {
		if (DBSparking.luckPerms != null) {
			return DBSparking.luckPerms.getUserManager().getUser(player.getUniqueId()).getNodes().stream()
					.anyMatch(node -> node.getKey().equalsIgnoreCase("group." + group) && node.getValue());
		} else if (DBSparking.perms != null) {
			return DBSparking.perms.playerInGroup(player, group);
		} else {
			return false;
		}
	}

	public void addPersonalBoost(String name, int amount, int time, String autor) {
		if (plugin.getBoostDataManager().personalBoostExists(getUuid(), name)) {
			return;
		}
		ActiveBoost boost = new ActiveBoost();
		boost.setBoostType(BoostType.PERSONAL);
		boost.setName(name);
		boost.setTarget(getUuid().toString());
		boost.setTargetName(player.getName());
		boost.setAmount(amount);
		boost.setAuthor(autor);
		boost.setDuration(time);
		boost.setLastUpdated(new Date());

		if (time != -1) {
			boost.setExpirationTimestamp(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(time));
		}

		int newBoostId = plugin.getBoostDataManager().addBoost(boost);
		if (newBoostId != -1) {
			Sound receiveSound = Sound.valueOf(plugin.getConfig().getString("sounds.boost_receive", "LEVEL_UP"));
			boost.setId(newBoostId);
			activeBoosts.add(boost);
			player.playSound(player.getLocation(), receiveSound, 1.0f, 1.0f);
			String timeStr = (time == -1) ? "Permanent" : NumberFormats.formatDuration(time);
			player.sendMessage(plugin.getLanguage().getMessage("boost_personal_received")
					.replace("%name%", boost.getName())
					.replace("%time%", timeStr));
		}
	}

	public void removePersonalBoost(String name) {
		ActiveBoost toRemove = null;
		for (ActiveBoost b : activeBoosts) {
			if (b.getName().equalsIgnoreCase(name) && b.getBoostType() == BoostType.PERSONAL) {
				toRemove = b;
				break;
			}
		}

		if (toRemove != null) {
			activeBoosts.remove(toRemove);
			plugin.getBoostDataManager().deletePersonalBoost(getUuid(), name); // elimina de DB
		}
	}

	public IDBCAddon getDBCAddon() {
		return (IDBCAddon) NpcAPI.Instance().getPlayer(player.getName()).getDBCPlayer();
	}

	private IPlayer getIPlayer() {
		try {
			if (player == null || !player.isOnline()) return null;
			return NpcAPI.Instance().getPlayer(player.getName());
		} catch (Throwable t) {
			return null;
		}
	}

	public String getQuestName(int questId) {
		IPlayer iPlayer = getIPlayer();
		if (iPlayer == null) return null;

		if (iPlayer.hasActiveQuest(questId)) {
			for (IQuest quest : iPlayer.getActiveQuests()) {
				if (quest.getId() == questId) {
					return quest.getName();
				}
			}
		}

		if (iPlayer.hasFinishedQuest(questId)) {
			for (IQuest quest : iPlayer.getFinishedQuests()) {
				if (quest.getId() == questId) {
					return quest.getName();
				}
			}
		}

		Quest quest = (Quest) noppes.npcs.controllers.QuestController.Instance.quests.get(questId);
		if (quest != null) {
			return quest.getName();
		}

		return null;
	}

	public void giveCustomItem(String internalName) {
		StatsItem statsItem = plugin.getItemDataManager().getItemByName(internalName);

		if (statsItem == null) {
			return;
		}

		org.bukkit.inventory.ItemStack itemStack = new org.bukkit.inventory.ItemStack(statsItem.getMaterial(), 1, (short) statsItem.getItemData());
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Text.color(statsItem.getDisplayName()));

		List<String> finalLore = new ArrayList<>();
		if (statsItem.getCustomLore() != null && !statsItem.getCustomLore().isEmpty()) {
			for (String line : statsItem.getCustomLore()) {
				finalLore.add(Text.color(line));
			}
		}
		List<String> statLore = plugin.getItemDataManager().getStatLoreFor(statsItem);
		if (!statLore.isEmpty()) {
			if (!finalLore.isEmpty()) {
				finalLore.add("");
			}
			finalLore.addAll(statLore);
		}

		if (!finalLore.isEmpty()) {
			finalLore.add("");
		}
		String rarityPath = "item_rarities." + statsItem.getRarity().name().toLowerCase();
		finalLore.add(plugin.getLanguage().getRawMessage(rarityPath));

		meta.setLore(finalLore);
		itemStack.setItemMeta(meta);

		itemStack = NBTEditor.setItemTag(itemStack, statsItem.getInternalName());

		player.getInventory().addItem(itemStack);
	}

	private String getSkillName(String skillName) {
		switch (skillName.toLowerCase(Locale.ROOT)) {
			case "fusion":
				return "Fusion";
			case "jump":
				return "Jump";
			case "dash":
				return "Dash";
			case "fly":
				return "Fly";
			case "endurance":
				return "Endurance";
			case "potentialunlock":
				return "PotentialUnlock";
			case "kisense":
				return "KiSense";
			case "meditation":
				return "Meditation";
			case "kaioken":
				return "Kaioken";
			case "godform":
				return "GodForm";
			case "mystic":
			case "oldkaiunlock":
				return "OldKaiUnlock";
			case "kiprotection":
				return "KiProtection";
			case "kifist":
				return "KiFist";
			case "kiboost":
				return "KiBoost";
			case "defensepenetration":
				return "DefensePenetration";
			case "kiinfuse":
				return "KiInfuse";
			case "ultrainstinct":
				return "UltraInstinct";
			case "instanttransmission":
				return "InstantTransmission";
			case "godofdestruction":
				return "GodOfDestruction";
			default:
				return null;
		}
	}

	public List<Integer> getUnlockedSoulSlots() {
		return unlockedSoulSlots;
	}

	public void setUnlockedSoulSlots(List<Integer> unlockedSoulSlots) {
		this.unlockedSoulSlots = unlockedSoulSlots;
	}

	/**
	 * Obtiene un objeto IQuest a partir de su ID.
	 * Busca en las misiones activas y completadas del jugador antes de buscar en la lista global.
	 * @param questId El ID de la misión.
	 * @return El objeto IQuest o null si no se encuentra.
	 */
	public IQuest getQuest(int questId) {
		IPlayer iPlayer = getIPlayer();
		if (iPlayer == null) return null;

		for (IQuest quest : iPlayer.getActiveQuests()) {
			if (quest.getId() == questId) {
				return quest;
			}
		}

		for (IQuest quest : iPlayer.getFinishedQuests()) {
			if (quest.getId() == questId) {
				return quest;
			}
		}

		return QuestController.Instance.quests.get(questId);
	}

	/**
	 * Obtiene una categoría de misiones por su ID.
	 * @param questID El ID de la misión de la cual se quiere obtener la categoría.
	 * @return El objeto IQuestCategory o null si no se encuentra.
	 */
	public IQuestCategory getQuestCategory(int questID) {
		IQuest quest = getQuest(questID);
		if (quest != null) {
			return quest.getCategory();
		}
		return null;
	}

	/**
	 * Obtiene una lista de todas las misiones dentro de una categoría específica.
	 * @param categoryId El ID de la categoría.
	 * @return Una lista de objetos IQuest. La lista estará vacía si la categoría no existe.
	 */
	public List<IQuest> getQuestsInCategory(int categoryId) {
		IQuestCategory category = getQuestCategory(categoryId);
		if (category != null) {
			return category.quests();
		}
		return new ArrayList<>();
	}

	/**
	 * Obtiene una lista con todos los IDs de las misiones de una categoría.
	 * @param categoryId El ID de la categoría.
	 * @return Una lista de Integers con los IDs de las misiones.
	 */
	public List<Integer> getQuestIdsInCategory(int categoryId) {
		return getQuestsInCategory(categoryId).stream()
				.map(IQuest::getId)
				.collect(Collectors.toList());
	}

	/**
	 * Verifica si el jugador tiene una misión activa.
	 * @param questId El ID de la misión.
	 * @return true si la misión está activa.
	 */
	public boolean isQuestActive(int questId) {
		IPlayer iPlayer = getIPlayer();
		return iPlayer != null && iPlayer.hasActiveQuest(questId);
	}

	/**
	 * Verifica si el jugador ha completado una misión previamente.
	 * @param questId El ID de la misión.
	 * @return true si la misión ha sido completada.
	 */
	public boolean isQuestCompleted(int questId) {
		IPlayer iPlayer = getIPlayer();
		return iPlayer != null && iPlayer.hasFinishedQuest(questId);
	}

	/**
	 * Obtiene el progreso de los objetivos de una misión activa en formato de texto.
	 * Muy útil para mostrar en UIs, mensajes u hologramas.
	 * @param questId El ID de la misión.
	 * @return Una lista de Strings, donde cada una representa un objetivo (ej: "Matar Zombies: 5/10").
	 * Devuelve una lista vacía si la misión no está activa o no tiene objetivos.
	 */
	public List<String> getQuestProgressText(int questId) {
		List<String> progressList = new ArrayList<>();
		IPlayer iPlayer = getIPlayer();
		if (iPlayer == null) {
			return progressList;
		}

		Quest quest = (Quest) getQuest(questId);
		if (quest == null) return progressList;

		for (IQuestObjective objective : quest.getObjectives(iPlayer)) {
			progressList.add(String.format("%s: %d/%d", objective.getText(), objective.getProgress(), objective.getMaxProgress()));
		}

		return progressList;
	}

	/**
	 * Verifica si una misión es de un tipo específico.
	 * @param type El tipo de misión a buscar ("kill", "item", "dialog", "location").
	 */
	public boolean questHasObjectiveType(int questId, String type) {
		Quest quest = (Quest) getQuest(questId);
		if (quest == null) return false;

		EnumQuestType questType;
		try {
			questType = EnumQuestType.valueOf(type.toUpperCase());
		} catch (IllegalArgumentException e) {
			return false;
		}
		return quest.type == questType;
	}

	public IQuestObjective[] getQuestObjectives(IPlayer player, int questId) {
		Quest quest = (Quest) getQuest(questId);
		if (quest == null) return new IQuestObjective[0];
		QuestInterface questInterface = (QuestInterface) quest.getQuestInterface();
		return questInterface.getObjectives((EntityPlayer)player.getMCEntity());
	}

	public String[] getFormattedQuestObjectives(int questId) {
		return getFormattedQuestObjectives(questId, false);
	}

	/**
	 * Obtiene una lista formateada de los objetivos de una misión activa.
	 * Utiliza Reflexión para acceder a datos no públicos y crear un texto personalizado.
	 * Formato: "1. [Objetivo] - ✅" o "1. [Objetivo] - ❌"
	 */
	public String[] getFormattedQuestObjectives(int questId, boolean includeStatusIcon) {
		List<String> formattedObjectives = new ArrayList<>();
		IPlayer iPlayer = getIPlayer();
		Quest quest = (Quest) getQuest(questId);

		if (iPlayer == null || quest == null) {
			return new String[0];
		}

		try {
			IQuestObjective[] objectives = getQuestObjectives(iPlayer, questId);
			int objectiveNumber = 1;

			if (objectives.length == 0) {
				formattedObjectives.add("&7No hay objetivos específicos.");
			}

			for (IQuestObjective objective : objectives) {
				String objectiveText = "";
				String statusIcon = objective.isCompleted() ? "&a✔" : "&c✖";
				String objectiveClassName = objective.getClass().getSimpleName();

			switch (objectiveClassName) {
				case "QuestDialogObjective": {
					Dialog dialog = (Dialog) getPrivateField(objective, "dialog");
					if (dialog != null) {
						objectiveText = String.format("&6%d. &e%s", objectiveNumber, dialog.title);
					}
					break;
				}
				case "QuestKillObjective": {
					String entityName = (String) getPrivateField(objective, "entity");
					if (entityName != null) {
						objectiveText = String.format("&6%d. &e%s &7(&r%d&7/&r%d&7)", objectiveNumber,
								plugin.getLanguage().getRawMessage("quest_kill_objective").replace("%target%", entityName), objective.getProgress(), objective.getMaxProgress());
					}
					break;
				}
				case "QuestItemObjective": {
					ItemStack nmsItemStack = (ItemStack) getPrivateField(objective, "questItem");
					if (nmsItemStack != null) {
						objectiveText = String.format("%d. %s &7(&r%d&7/&r%d&7)", objectiveNumber,
								plugin.getLanguage().getRawMessage("quest_item_objective").replace("%item%", nmsItemStack.getDisplayName()), objective.getProgress(), objective.getMaxProgress());
					}
					break;
				}
				case "QuestLocationObjective": {
					String locationName = (String) getPrivateField(objective, "location");
					if (locationName != null) {
						objectiveText = String.format("%d. %s", objectiveNumber, plugin.getLanguage().getRawMessage("quest_location_objective").replace("%location%", locationName));
					}
					break;
				}
			}

				if (!objectiveText.isEmpty()) {
					if (includeStatusIcon) {
						formattedObjectives.add(objectiveText + " | " + statusIcon);
					} else {
						formattedObjectives.add(objectiveText);
					}
					objectiveNumber++;
				}
			}
		} catch (Exception e) {
			plugin.getLogger().warning("Error al procesar objetivos para la misión " + questId + ": " + e.getMessage());
			formattedObjectives.add("");
			e.printStackTrace();
		}
		return formattedObjectives.toArray(new String[0]);
	}

	/**
	 * Obtiene una lista de todas las misiones que el jugador puede aceptar
	 * DENTRO DE UNA CATEGORÍA DE DIÁLOGOS ESPECÍFICA.
	 *
	 * @param categoryId El ID de la categoría de diálogos donde se buscarán las misiones.
	 * @return Una lista de objetos IQuest con las misiones disponibles.
	 */
	public List<IQuest> getAvailableQuests(int categoryId) {
		List<IQuest> availableQuests = new ArrayList<>();
		IPlayer iPlayer = getIPlayer();
		IDialogCategory dialogCategory = getDialogCategory(categoryId);

		if (iPlayer == null || dialogCategory == null) {
			return availableQuests;
		}
		for (Dialog dialog : ((DialogCategory) dialogCategory).dialogs.values()) {
			if (dialog.hasQuest() && dialog.availability.isAvailable(iPlayer)) {
				IQuest quest = dialog.getQuest();
				availableQuests.add(quest);
			}
		}
		return availableQuests;
	}


	public List<Integer> getAvailableQuestIds(int categoryId) {
		return getAvailableQuests(categoryId).stream()
				.map(IQuest::getId)
				.collect(Collectors.toList());
	}

	/**
	 * Verifica si el jugador puede aceptar una misión específica.
	 * <p>
	 * Busca si existe al menos un diálogo que inicie esta misión y cuyos requisitos
	 * de disponibilidad sean cumplidos por el jugador.
	 *
	 * @param questId El ID de la misión a comprobar.
	 * @return true si el jugador puede empezar la misión a través de al menos un diálogo.
	 */
	public boolean canAcceptQuest(int questId) {
		IPlayer iPlayer = getIPlayer();
		if (iPlayer == null) {
			return false;
		}

		IQuest questToCheck = getQuest(questId);
		if (questToCheck == null) return false;

		if (isQuestActive(questId) || (isQuestCompleted(questId) && questToCheck.getRepeatType() == 0)) {
			return false;
		}

		for (Dialog dialog : DialogController.Instance.dialogs.values()) {
			if (dialog.hasQuest() && dialog.getQuest().getId() == questId) {
				if (dialog.availability.isAvailable(iPlayer)) {
					return true; // Encontramos una forma de obtener la misión.
				}
			}
		}

		return false;
	}

	public boolean checkAvailability(int dialogId) {
		IPlayer iPlayer = getIPlayer();
		if (iPlayer == null || dialogId == 0) return false;
		Dialog dialog = DialogController.Instance.dialogs.get(dialogId);
		if (dialog == null) return false;
		return dialog.availability.isAvailable(iPlayer);
	}

	public boolean checkQuestAvailability(int questId) {
		IPlayer iPlayer = getIPlayer();
		if (iPlayer == null || questId == 0) return false;

		IQuest questToCheck = getQuest(questId);
		if (questToCheck == null) return false;

		for (Dialog dialog : DialogController.Instance.dialogs.values()) {
			if (dialog.hasQuest() && dialog.getQuest().getId() == questId) {
				if (dialog.availability.isAvailable(iPlayer)) {
					return true;
				}
			}
		}

		return isQuestActive(questId) || isQuestCompleted(questId) && questToCheck.getRepeatType() != 0;
	}

	public boolean canStartQuest(int questId) {
		IPlayer iPlayer = getIPlayer();
		if (iPlayer == null || questId == 0) return false;

		IQuest questToCheck = getQuest(questId);
		if (questToCheck == null) return false;

		if (isQuestCompleted(questId) && questToCheck.getRepeatType() == 0) {
			return false;
		}

		if (canAcceptQuest(questId)) {
			return true;
		}

		return isQuestActive(questId);
	}

	public IDialog getDialogWithQuest(int questId) {
		for (Dialog dialog : DialogController.Instance.dialogs.values()) {
			if (dialog.hasQuest() && dialog.getQuest().getId() == questId) {
				return dialog;
			}
		}
		return null;
	}

	/**
	 * Obtiene el texto de un diálogo, completamente limpio, formateado y dividido en líneas.
	 * Este métodos se encarga de traducir los códigos de color '&' y de limpiar los saltos de línea.
	 * @param dialogId El ID del diálogo a procesar.
	 * @return Un array de strings (String[]) listo para ser usado en la GUI.
	 */
	public String getCleanDialogText(int dialogId) {
		IDialog dialog = getDialog(dialogId);
		if (dialog == null) {
			return "&cDiálogo " + dialogId + " no encontrado.";
		}

		String rawText = dialog.getText();
		if (rawText == null || rawText.isEmpty()) {
			return "";
		}
		String cleanText = rawText.replace("\r", "");

		return cleanText;
	}

	/**
	 * Obtiene un objeto IDialog a partir de su ID.
	 * @param dialogId El ID del diálogo.
	 * @return El objeto IDialog o null si no se encuentra.
	 */
	public IDialog getDialog(int dialogId) {
		return DialogController.Instance.dialogs.get(dialogId);
	}

	/**
	 * Obtiene una categoría de diálogos por su ID.
	 * @param categoryId El ID de la categoría.
	 * @return El objeto IDialogCategory o null si no se encuentra.
	 */
	public IDialogCategory getDialogCategory(int categoryId) {
		return DialogController.Instance.categories.get(categoryId);
	}

	/**
	 * Obtiene una lista de todos los diálogos que el jugador puede ver en este momento
	 * DENTRO DE UNA CATEGORÍA ESPECÍFICA.
	 *
	 * @param categoryId El ID de la categoría de diálogos a consultar.
	 * @return Una lista de objetos IDialog con los diálogos disponibles.
	 */
	public List<IDialog> getAvailableDialogs(int categoryId) {
		List<IDialog> availableDialogs = new ArrayList<>();
		IPlayer iPlayer = getIPlayer();
		IDialogCategory dialogCategory = getDialogCategory(categoryId);

		if (iPlayer == null || dialogCategory == null) {
			return availableDialogs;
		}

		for (Dialog dialog : ((DialogCategory) dialogCategory).dialogs.values()) {
			if (dialog.availability.isAvailable(iPlayer)) {
				availableDialogs.add(dialog);
			}
		}
		return availableDialogs;
	}

	public List<Integer> getAvailableDialogIds(int categoryId) {
		return getAvailableDialogs(categoryId).stream()
				.map(IDialog::getId)
				.collect(Collectors.toList());
	}

	public IAvailability createNewAvailability() {
		return new Availability();
	}

	/**
	 * Métodos privado para obtener el objeto Party del jugador.
	 */
	public IParty getParty() {
		IPlayer iPlayer = getIPlayer();
		if (iPlayer == null) {
			return null;
		}

		PlayerData playerData = PlayerData.get((net.minecraft.entity.player.EntityPlayer) iPlayer.getMCEntity());

		if (playerData == null || playerData.partyUUID == null) {
			return null;
		}

		return (IParty) PartyController.Instance().getParty(playerData.partyUUID);
	}

	/**
	 * Verifica si el jugador se encuentra actualmente en una party.
	 */
	public boolean isInParty() {
		return getParty() != null;
	}

	/**
	 * Obtiene el nombre del líder de la party del jugador.
	 */
	public String getPartyLeader() {
		IParty party = getParty();
		if (party != null) {
			return plugin.getLanguage().getRawMessage("party_leader_name")
					.replace("%leader%", party.getPartyLeaderName());
		}
		return null;
	}

	/**
	 * Obtiene el número de miembros en la party del jugador.
	 */
	public int getPartySize() {
		IParty party = getParty();
		if (party != null) {
			return party.getPlayerNamesList().size();
		}
		return 0;
	}

	/**
	 * Obtiene una lista con los nombres de todos los miembros de la party.
	 */
	public List<String> getPartyMembers() {
		IParty party = getParty();
		if (party != null) {
			return party.getPlayerNamesList();
		}
		return new ArrayList<>();
	}

	/**
	 * Helper para obtener el valor de un campo privado de cualquier objeto.
	 * @param obj El objeto del que se leerá el campo.
	 * @param fieldName El nombre del campo a leer.
	 * @return El valor del campo, o null si ocurre un error.
	 */
	private Object getPrivateField(Object obj, String fieldName) {
		try {
			Field field = obj.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(obj);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ScriptPlayer getScriptPlayer() {
		try {
			if (player == null || !player.isOnline()) return null;
			return (ScriptPlayer) getIPlayer();
		} catch (Throwable t) {
			return null;
		}
	}
}