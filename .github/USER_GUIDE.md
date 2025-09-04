<p align="right">
  <a href="./GUIA_DE_USUARIO.md">Español 🇪🇸</a>
</p>

# 📖 DBSparking Plugin Guide

Welcome to the official **_DBSparking!_** guide. Here you will find a complete list of all the features, commands, and placeholders the plugin offers.

## ✨ Placeholders (PlaceholderAPI)

To use these variables, you need to have [PlaceholderAPI v2.11.1](https://www.spigotmc.org/resources/placeholderapi.6245/) installed.

*`%dbsparking_dbclvl%`* -> Displays the player's power level, calculated from their stats.  
*`%dbsparking_dbcrace%`* -> Displays the player's race in text format (e.g., "Saiyan").  
*`%dbsparking_dbcclass%`* -> Displays the player's class in text format (e.g., "Warrior").  
*`%dbsparking_dbctps%`* -> Displays the player's Training Points, formatted with thousand separators.   
*`%dbsparking_dbcstr%`, `dex`, `con`, `wil`, `mnd`, `spi`* -> Display each stat formatted with thousand separators.  
*`%dbsparking_total_boost%`* -> Displays the total TP bonus percentage the player is receiving from all sources.  
*`%dbsparking_personal_boost%`* -> Displays the highest personal booster the player has, with its remaining time.  
*`%dbsparking_global_boost%`* -> Displays the highest active global booster on the server.  
*`%dbsparking_rank_boost%`* -> Displays the highest rank booster affecting the player.

> **Annotation:** The booster placeholders are designed to always show the one with the highest value if multiple of the same type are active, ensuring the information on scoreboards is always the most relevant.

> **Annotation 2:** Do you think a placeholder is missing? Do you have a suggestion? Don't hesitate to open an issue on the GitHub repository! Your feedback is very valuable for improving the plugin.
---
## 💻 Commands

> **Note:** Arguments in `<>` are **required**. Those in `[]` are **optional**.

### 🔧 General Commands

* `/dbsp help [boost|item]`
    * **Function:** Shows the list of available commands. If a category (`boost` or `item`) is specified, it only shows commands for that category.
* `/dbsp reload`
    * **Function:** Reloads all plugin configuration and language files without needing a server restart.
* `/dbsp tps <amount> [player] [applyCombo]`
    * **Function:** Grants the specified `<amount>` of Training Points to the player. If no `[player]` is specified, you grant them to yourself. This command automatically applies all active boosters affecting the player.
    * `[applyCombo]`: If set to `true`, it will apply the combo bonus based on the player's current combo count (see config). Defaults to `true`.
* `/dbsp souls [player]`
    * **Function:** Opens the player's Souls menu. If no `[player]` is specified, it opens the menu for the player executing the command.

### ⚠️ Player Data Management

> **WARNING!** These commands are destructive and **PERMANENT**. They modify the save files of other mods. Use them with extreme caution and always ensure you have a backup.

* `/dbsp data delete <player> <dbc|npc|all>`
    * **Function:** Initiates a request to delete a player's data. You must specify the data type:
        * `dbc`: Deletes the player's `.dat` file, which resets their stats, TPs, character, etc. **This also deletes their inventory; it is recommended to save it in a chest beforehand.**
        * `npc`: Deletes the player's `.json` file from the CustomNPCs mod, thus removing dialogues they have read, quests they have completed, etc.
        * `all`: Performs both actions.
    * **Annotation:** This command does not delete anything immediately but will ask for confirmation.
* `/dbsp data confirm`
    * **Function:** Confirms a previously initiated data deletion request. You have **10 seconds** to use this command after using `/dbsp data delete`. Otherwise, the command is automatically cancelled.
    * For security, the affected player will be kicked from the server and will need to log back in to continue playing.

### 🚀 Booster Commands

> **Note:** `<amount>` is always a percentage (e.g., `30` for a 30% increase `[x1.3]`, `500` for a 500% increase `[x5]`). `<time>` is always in seconds (e.g., `600` for 10 minutes). Use `-1` for `<time>` to make a booster permanent.

* `/dbsp boost add <type> <name> ...`
    * **Function:** Adds a new booster. The `type` determines the subsequent arguments:
    * `global <name> <amount> <time> [author]`: Creates a global booster. The `<name>` must be unique.
    * `personal <name> <player> <amount> <time> [author]`: Creates a booster for a specific player. The `<name>` can be repeated for different players.
    * `rank <name> <rank> <amount> <time> [author]`: Creates a booster for a specific rank. The `<name>` must be unique.
* `/dbsp boost delete <name> [player]`
    * **Function:** Deletes a booster. If the booster is of `personal` type, specifying the `[player]` is **mandatory**.
* `/dbsp boost list`
    * **Function:** Displays a list of all active boosters on the server, including their type, name, amount, and remaining duration.

### ⚔️ Item Commands
> **Note:** Adding stats to an item automatically creates lore lines describing those stats. These lines cannot be edited or removed manually as they are managed by the plugin's database.

* `/dbsp item create <internalName> <type> "<displayName>" [rarity]`
    * **Function:** Creates a new custom item in the database, based on the item you are holding in your hand.
    * `<internalName>`: The unique ID for the item. Cannot contain spaces.
    * `<type>`: Can be `WEAPON`, `ARMOR` or `SOUL`.
    * `"<displayName>"`: The visible name of the item. **Must be enclosed in quotes** to allow for spaces and color codes (`&`).
    * `[rarity]`: Optional. The rarity of the item. Can be `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, `LEGENDARY`. Defaults to `COMMON`.
* `/dbsp item lore <internalName> <line> <add|edit|delete>`
    * **Function:** Manages the custom lore. After running the command with `add` or `edit`, the plugin will prompt you to type the lore line directly in chat.
* `/dbsp item edit <internalName> [property:value]...`
    * **Function:** Edits the properties of an existing item. You can modify multiple properties at once.
    * **Properties:** `name:<newName>`, `type:<newType>`, `display:"<newDisplayName>"`, `rarity:<newRarity>`.
* `/dbsp item stat <internalName> <stat> <add|multiply|percent|remove> [value] "[bonusID]"`
    * **Function:** Manages the stat bonuses of an item.
    * `<stat>`: `STR`, `DEX`, `CON`, `WIL`, `MND`, `SPI`. It is not case-sensitive.
    * `<add|multiply|percent>`: The type of bonus. `add` can use negative values to subtract. `percent 20` results in a 20% increase of the player's current stat (it updates dynamically).
    * `"[bonusID]"`: An optional, custom ID for the bonus, enclosed in quotes. It can contain spaces and color codes (`§`). A default ID is generated if not specified. This ID is shown when hovering over the stat in the DBC Menu (`V`).
    * `remove` is used to delete an existing bonus, not to add a bonus that subtracts stats.
* `/dbsp item condition <internalName> <add|remove> <level|rank> [options]`
    * **Function:** Sets the conditions required to use an item.
    * **Level Options:** `min:<value>`, `max:<value>`. These are optional and can be used together or separately.
    * **Rank Options:** `rank <name>`.
* `/dbsp item list`
    * **Function:** Displays an interactive list of all created custom items.
* `/dbsp item info <internalName>`
    * **Function:** Shows a breakdown of an item's stats. This is executed when clicking `[Stats]` in the list.
* `/dbsp item give <internalName> [player]`
    * **Function:** Gives you a copy of the custom item. This copy is already tagged with the necessary NBT data for the plugin to recognize it.
* `/dbsp item delete <internalName>`
    * **Function:** Permanently deletes a custom item from the database.

> **Annotation:** Do you think we could add more commands? Do you have a suggestion? Don't hesitate to open an issue on the GitHub repository! Your feedback is very valuable for improving the plugin.