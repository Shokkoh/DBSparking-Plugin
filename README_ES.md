<p align="right">
  <a href="./README.md">English 🇺🇸</a>
</p>

# ✨ DBSparking plugin - v1.0

Un plugin "Todo en Uno" para servidores de Minecraft 1.7.10 que usan mods como DragonBlockC y CustomNPCs, enfocado en añadir nuevas funcionalidades y herramientas administrativas.

**✍️ Autores:** [Shokkoh](https://github.com/Shokkoh)

---

## ⚙️ Funcionalidades Principales

Este plugin integra una variedad de sistemas para mejorar la experiencia de juego y la administración del servidor:

* 🚀 **Sistema de Boosters:**
    * **Boosters Personales:** Otorga bonus de TP a jugadores específicos por un tiempo determinado. El temporizador solo se descuenta si el jugador está en línea.
    * **Boosters Globales:** Activa un bonus de TP para todos los jugadores del servidor.
    * **Boosters por Rango:** Proporciona un bonus de TP exclusivo para jugadores con un rango específico (requiere LuckPerms o Vault).
    * Los boosters son persistentes y se guardan en una base de datos (SQLite/MySQL).

* ⚔️ **Sistema de Ítems con Estadísticas:**
    * Crea ítems con estadísticas de DBC personalizadas (STR, DEX, CON, etc.).
    * Añade valores fijos (`+100`), multiplicadores (`x1.5`), o bonus porcentuales (`+10%`).
    * Define requisitos de **nivel** o **rango** para poder equipar los ítems.
    * Especifica si un ítem otorga sus stats solo al sostenerlo (armas) o en las ranuras de armadura (armaduras).
    * Añade lore personalizado a cada ítem.
    * Sistema anti-drop para evitar que los ítems sean transferidos sin permiso.

* 🔧 **Gestión de Datos de Jugador:**
    * Comando `/dbsp data` para eliminar datos de jugador de otros mods (DBC, CustomNPCs), ideal para resolver bugs o limpiar perfiles.
    * Sistema de confirmación para evitar borrados accidentales.

* 🎁 **Sistema de Ítems con Comandos:**
    * Crea ítems que ejecutan comandos al ser usados o clickeados con el botón derecho.
    * Define nombres personalizados, lores, daños y mensajes para estos ítems.
    * Incluye una GUI para facilitar la creación de ítems.

* ⌨️ **Comando Ejecutar Como Jugador:**
    * `/dbsp eap <jugador> <comando>` para ejecutar comandos como otro jugador, útil para tareas administrativas.
    * También puede enviar mensajes de chat como el jugador objetivo, y ejecutar comandos incluso si el jugador no tiene permiso para ese comando.

* 🔒 **Sistema de Auto-Login:**
    * Permite a los jugadores iniciar sesión automáticamente después de su primer login manual si vinculan su dirección IP con su Nickname y demuestran que son los dueños de la cuenta ingresando su contraseña.
    * Muy útil para servidores que no usan modo online, ya que previene el robo de cuentas.
    * Si el jugador cambia su IP, o alguien intenta unirse con sus cuentas, necesitará iniciar sesión manualmente nuevamente.

* 🔗 **Integraciones:**
    * **PlaceholderAPI:** Expone una gran variedad de datos (stats de DBC, boosters activos, etc.) para ser usados en otros plugins como scoreboards o tabs.
    * **AuthMe/AuthMeReloeaded:** Integra el sistema de Auto-Login con AuthMe, permitiendo a los jugadores iniciar sesión automáticamente después de su primer login manual.
    * **LuckPerms/Vault:** Integra el sistema de Boosters por Rango con estos plugins para proporcionar bonus de TP exclusivos basados en los rangos de los jugadores.

---

## ✅ Requisitos

Para que DBSparking funcione correctamente, tu servidor necesita tener instalados los siguientes plugins y mods:

1.  **Servidor:** [Crucible](https://github.com/CrucibleMC/Crucible), [Thermos](https://github.com/CyberdyneCC/Thermos), o cualquier implementación de servidor CraftBukkit/Spigot-Forge compatible con **Minecraft 1.7.10**.
2.  **Mod:** **[CustomNPCs+](https://github.com/KAMKEEL/CustomNPC-Plus)** de [Kamkeel](https://github.com/KAMKEEL).
3.  **Mod:** **[CNPC-DBC Addon](https://github.com/KAMKEEL/CustomNPC-DBC-Addon)** de [Kamkeel](https://github.com/KAMKEEL).
4.  **Mod:** **[DragonBlock C](https://www.curseforge.com/minecraft/mc-mods/jingames-dragon-block-c)** de JinGames.
5.  **Plugin (Opcional):** **[LuckPerms](https://luckperms.net/) o [Vault](https://www.spigotmc.org/resources/vault.34315/)** para usar las funcionalidades de Rango.
6.  **Plugin (Opcional):** **[PlaceholderAPI v2.11.1](https://www.spigotmc.org/resources/placeholderapi.6245/update?update=437598)** para usar los placeholders.
7. **Plugin (Opcional):** **[AuthMe/AuthMeReloaded v5.3.2](https://www.spigotmc.org/resources/authme-reloaded.6269/)** para usar el sistema de Auto-Login.