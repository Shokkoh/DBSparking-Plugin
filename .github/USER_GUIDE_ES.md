<p align="right">
  <a href="./USER_GUIDE.md">English 🇺🇸</a>
</p>

# 📖 Guía del Plugin DBSparking

Bienvenido a la guía oficial de **_DBSparking!_**. Aquí encontrarás una lista completa de todas las funcionalidades, comandos y placeholders que el plugin ofrece.

## ✨ Placeholders (PlaceholderAPI)

Para usar estas variables, necesitas tener instalado [PlaceholderAPI v2.11.1](https://www.spigotmc.org/resources/placeholderapi.6245/).

*`%dbsparking_dbclvl%`* -> Muestra el nivel de poder del jugador, calculado a partir de sus stats.  
*`%dbsparking_dbcrace%`* -> Muestra la raza del jugador en formato de texto (ej: "Saiyan").  
*`%dbsparking_dbcclass%`* -> Muestra la clase del jugador en formato de texto (ej: "Guerrero").  
*`%dbsparking_dbctps%`* -> Muestra los Training Points del jugador, formateados con separadores de miles.  
*`%dbsparking_dbcstr%`, `dex`, `con`, `wil`, `mnd`, `spi`* -> Muestran cada estadística formateada con separadores de miles.  
*`%dbsparking_total_boost%`* -> Muestra el porcentaje total de bonus de TP que el jugador está recibiendo de todas las fuentes.  
*`%dbsparking_personal_boost%`* -> Muestra el booster personal más alto que tiene el jugador, con su tiempo restante.  
*`%dbsparking_global_boost%`* -> Muestra el booster global más alto activo en el servidor.  
*`%dbsparking_rank_boost%`* -> Muestra el booster de rango más alto que afecta al jugador.  

> **Anotación:** Los placeholders de boosters están diseñados para mostrar siempre el de mayor valor si hay varios activos del mismo tipo, para que la información en scoreboards sea siempre la más relevante.

> **Anotación 2:** ¿Crees que falta algún placeholder? ¿Tienes alguna sugerencia? ¡No dudes en abrir un issue en el repositorio de GitHub! Tu feedback es muy valioso para mejorar el plugin.
---
## 💻 Comandos

> **Nota:** Los argumentos entre `<>` son **obligatorios**. Los que están entre `[]` son **opcionales**.

### 🔧 Comandos Generales

* `/dbsp help [boost|item]`
    * **Función:** Muestra la lista de comandos disponibles. Si se especifica una categoría (`boost` o `item`), muestra solo los comandos de esa categoría.
* `/dbsp reload`
    * **Función:** Recarga todos los archivos de configuración y de idiomas del plugin sin necesidad de reiniciar el servidor.
* `/dbsp tps <cantidad> [jugador] [aplicarCombo]`
    * **Función:** Otorga la `<cantidad>` especificada de Training Points al jugador. Si no se especifica un `[jugador]`, te los otorgas a ti mismo. Este comando aplica automáticamente todos los boosters activos que afecten al jugador.
    * `[aplicarCombo]`: Si se establece en `true`, aplicará el bonus de combo basado en la cantidad de combos actual del jugador (ver configuración). Por defecto es `true`.

* `/dbsp souls [jugador]`
    * **Función:** Abre el menú de Almas del jugador. Si no se especifica un `[jugador]`, se abre el menú del jugador que ejecuta el comando.
  
### ⚠️ Gestión de Datos de Jugador (PlayerData)

> **¡ADVERTENCIA!** Estos comandos son destructivos y **PERMANENTES**. Modifican los archivos de guardado de otros mods. Úsalos con extrema precaución y asegúrate de siempre tener algún respaldo/backup.

* `/dbsp data delete <jugador> <dbc|npc|all>`
    * **Función:** Inicia una solicitud para eliminar los datos de un jugador. Debes especificar el tipo de datos:
        * `dbc`: Borra el archivo `.dat` del jugador, lo que resetea sus stats, TPs, personaje, etc. **También elimina su inventario, se recomienta guardarlo en un Cofre antes.**
        * `npc`: Borra el archivo `.json` del jugador del mod CustomNPCs, eliminando así los Dialogos que haya leído, las Misiones que haya completado, etc.
        * `all`: Ejecuta ambas acciones.
    * **Anotación:** Este comando no borra nada inmediatamente, sino que te pedirá confirmación.
* `/dbsp data confirm`
    * **Función:** Confirma una solicitud de borrado de datos iniciada previamente. Tienes **10 segundos** para escribir este comando después de usar `/dbsp data delete`. En caso contrario, el comando se cancela automáticamente.
    * Por seguridad, el jugador afectado será kickeado del servidor y deberá ingresar nuevamente para continuar jugando.

### 🚀 Comandos de Boosters

> **Nota:** `<amount>` es siempre un porcentaje (ej: `30` para un 30% `[x1.3]`, `500` para un 500% `[x5]`). `<time>` es siempre en segundos (ej: `600` para 10 minutos). Usa `-1` en `<time>` para un booster permanente.

* `/dbsp boost add <type> <name> ...`
    * **Función:** Añade un nuevo booster. El `type` determina los siguientes argumentos:
    * `global <name> <amount> <time> [autor]`: Crea un booster global. El `<name>` debe ser único.
    * `personal <name> <player> <amount> <time> [autor]`: Crea un booster para un jugador. El `<name>` puede repetirse entre diferentes jugadores.
    * `rank <name> <rank> <amount> <time> [autor]`: Crea un booster para un rango. El `<name>` debe ser único.
* `/dbsp boost delete <name> [player]`
    * **Función:** Elimina un booster. Si el booster es de tipo `personal`, es **obligatorio** especificar el `[player]`.
* `/dbsp boost list`
    * **Función:** Muestra una lista de todos los boosters activos en el servidor, su tipo, nombre, cantidad y duración restante.

### ⚔️ Comandos de Ítems  
> **Nota:** Agregar estadísticas a un item, crea automáticamente líneas de lore que describen dichas estadísticas. Estas líneas no se pueden editar ni eliminar manualmente, ya que son gestionadas por la base de datos del plugin.

* `/dbsp item create <internalName> <type> "<displayName>" [rarity]`
    * **Función:** Crea un nuevo ítem personalizado en la base de datos, basándose en el ítem que sostienes en la mano.
    * `<internalName>`: Es el ID único del ítem. No puede contener espacios.
    * `<type>`: Puede ser `WEAPON` (arma), `ARMOR` (armadura) o `SOUL` (alma).
    * `"<displayName>"`: Es el nombre visible del ítem. **Debe ir entre comillas** para permitir espacios y códigos de color (`&`).
    * `[rarity]`: Opcional. Define la rareza del ítem. Puede ser `COMMON`, `UNCOMMON`, `RARE`, `EPIC` o `LEGENDARY`. Si no se especifica, se asigna `COMMON` por defecto.
* `/dbsp item lore <internalName> <linea> <add|edit|delete>`
    * **Función:** Gestiona el lore personalizado. Tras ejecutar el comando con `add` o `edit`, el plugin te pedirá que escribas la línea de texto directamente en el chat.
* `/dbsp item edit <internalName> [propiedad:valor]...`
    * **Función:** Edita las propiedades de un ítem ya creado. Puedes modificar múltiples propiedades a la vez.
    * **Propiedades:** `name:<nuevoNombre>`, `type:<nuevoTipo>`, `display:"<nuevoDisplay>"`, `rarity:<nuevaRareza>`.
* `/dbsp item stat <internalName> <stat> <add|multiply|percent|remove> [valor] "[bonusID]"`
    * **Función:** Gestiona los bonus de estadísticas de un ítem.
    * `<stat>`: `STR`, `DEX`, `CON`, `WIL`, `MND`, `SPI`. No es sensible a mayúsculas o minúsculas.
    * `<add|multiply|percent>`: Tipo de bonus. `add` puede usar valores negativos para restar. `percent 20` resulta en un aumento del 20% de la estadística actual del jugador (se va actualizando dinámicamente).
    * `"[bonusID]"`: Un ID personalizado opcional y entre comillas para el bonus, que puede contener espacios y colores (`§`). Si no se especifica, se genera uno por defecto. Este ID es el mostrado al colocar el cursor sobre la estadística dentro del Menú de DBC (`V`).
    * `remove` se utiliza para eliminar un bonus existente. No para agregar un bonus que reste estadísticas.
* `/dbsp item condition <internalName> <add|remove> <level|rank> [opciones]`
    * **Función:** Establece las condiciones para que un ítem pueda ser usado.
    * **Opciones de Nivel:** `min:<valor>`, `max:<valor>`. Son opcionales y se pueden usar juntas o por separado.
    * **Opciones de Rango:** `rank <nombre>`.
* `/dbsp item list`
    * **Función:** Muestra una lista interactiva de todos los ítems creados.
* `/dbsp item info <internalName>`
    * **Función:** Muestra un desglose de las estadísticas de un ítem. Se ejecuta al hacer clic en `[Stats]` en la lista.
* `/dbsp item give <internalName> [jugador]`
    * **Función:** Te da una copia del ítem personalizado. Esta copia ya viene con la etiqueta NBT necesaria para que el plugin la reconozca.
* `/dbsp item delete <internalName>`
    * **Función:** Elimina un ítem personalizado de la base de datos de forma permanente.

> **Anotación:** ¿Crees que podríamos agregar más comandos? ¿Tienes alguna sugerencia? ¡No dudes en abrir un issue en el repositorio de GitHub! Tu feedback es muy valioso para mejorar el plugin.