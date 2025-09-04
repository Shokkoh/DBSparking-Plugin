<p align="right">
  <a href="./README_ES.md">Español 🇪🇸</a>
</p>

# ✨ DBSparking plugin - v1.0

An "All-In-One" plugin for Minecraft 1.7.10 servers running mods like DragonBlockC and CustomNPCs, focused on adding new features and administrative tools.

**✍️ Author:** [Shokkoh](https://github.com/Shokkoh)

---

## ⚙️ Core Features

This plugin integrates a variety of systems to enhance the gameplay experience and server administration:

* 🚀 **Booster System:**
    * **Personal Boosters:** Grant TP bonuses to specific players for a set duration. The timer only counts down when the player is online.
    * **Global Boosters:** Activate a TP bonus for all players on the server.
    * **Rank Boosters:** Provide an exclusive TP bonus for players with a specific rank (requires LuckPerms or Vault).
    * Boosters are persistent and saved in a database (SQLite/MySQL).

* ⚔️ **Stats Item System:**
    * Create items with custom DBC stat bonuses (STR, DEX, CON, etc.).
    * Add fixed values (`+100`), multipliers (`x1.5`), or percentage-based bonuses (`+10%`).
    * Define **level** or **rank** requirements to equip items.
    * Specify whether an item grants stats only when held (weapons) or in armor slots (armors).
    * Add custom lore to each item.
    * Anti-drop system to prevent items from being transferred without permission.

* 🔧 **Player Data Management:**
    * `/dbsp data` command to delete player data from other mods (DBC, CustomNPCs), ideal for fixing bugs or clearing profiles.
    * Confirmation system to prevent accidental deletions.
  
* 🎁 **Custom Items System:**
    * Create items that execute commands when used or right-clicked.
    * Define custom names, lores, damage and messages for these items.
    * Includes a GUI for easy item creation.
  
* ⌨️ **Execute As Player Command:**
    * `/dbsp eap <player> <command>` to run commands as another player, useful for administrative tasks.
    * Also can send chat messages as the target player, and execute commands even if the player doesn't have permission to that command.
  
* 🔐 **Auto-Login System:**
    * Allows players to log in automatically after their first manual login if they link their IP address with their Nickname and prove they own the account by imputing their password.
    * Very useful for servers that don't use online mode, as it prevents account theft.
    * If the player changes their IP, or someone tries to join with their accounts, they will need to log in manually again.

* 🔗 **Integrations:**
    * **PlaceholderAPI:** Exposes a wide variety of data (DBC stats, active boosters, etc.) for use in other plugins like scoreboards or tabs.
    * **AuthMe/AuthMeReloeaded:** Integrates the Auto-Login system with AuthMe, allowing players to log in automatically after their first manual login.
    * **LuckPerms/Vault:** Integrates the Rank Booster system with these plugins to provide exclusive TP bonuses based on player ranks.
---

## ✅ Requirements

For DBSparking to function correctly, your server must have the following plugins and mods installed:

1.  **Server:** [Crucible](https://github.com/CrucibleMC/Crucible), [Thermos](https://github.com/CyberdyneCC/Thermos), or any CraftBukkit/Spigot-Forge server implementation compatible for **Minecraft 1.7.10**.
2.  **Mod:** **[CustomNPCs+](https://github.com/KAMKEEL/CustomNPC-Plus)** from [Kamkeel](https://github.com/KAMKEEL).
3.  **Mod:** **[CNPC-DBC Addon](https://github.com/KAMKEEL/CustomNPC-DBC-Addon)** from [Kamkeel](https://github.com/KAMKEEL).
4.  **Mod:** **[DragonBlock C](https://www.curseforge.com/minecraft/mc-mods/jingames-dragon-block-c)** from JinGames.
5.  **Plugin (Optional):** **[LuckPerms](https://luckperms.net/) or [Vault](https://www.spigotmc.org/resources/vault.34315/)** to use the Rank features.
6.  **Plugin (Optional):** **[PlaceholderAPI v2.11.1](https://www.spigotmc.org/resources/placeholderapi.6245/update?update=437598)** to use the placeholders.
7. **Plugin (Optional):** **[AuthMeReloaded v5.3.2](https://www.spigotmc.org/resources/authme-reloaded.6269/)** to use the Auto-Login system.