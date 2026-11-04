# CustomNametag

A modern, high-performance Minecraft nametag plugin for Paper 1.21.x servers using Minecraft Display Entities (`TextDisplay`).

Developed by **u5dt**.

---

## Features

- **Display Entities:** Utilizes vanilla `TextDisplay` entities riding the player, providing smooth, high-fidelity nametags without client-side modifications.
- **LuckPerms Integration:** Automatic prefix and primary group support.
- **PlaceholderAPI Integration:** Full support for any PlaceholderAPI placeholders.
- **Ping Display:** Real-time ping counter on the second line.
- **Player Customization:**
  - Toggle nametag visibility per-player.
  - Custom background color support (`transparent`, `none`, `default`, hex `#RRGGBB`, and standard colors).
  - Text shadow toggle.
- **Optimized & Safe:** Automatically hides the player's own nametag in first-person view, handles world changes and teleports, and cleans up all entities gracefully on server reload or shutdown.

---

## Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/nametag visibility` | Toggle seeing other players' nametags | `customnametag.use` |
| `/nametag background <color>` | Change nametag background color | `customnametag.use` |
| `/nametag textshadow` | Toggle text shadow on your nametag | `customnametag.use` |
| `/nametag reload` | Reload the configuration | `customnametag.admin` |

**Aliases:** `/cnt`

---

## Building from Source

Requires **Java 21+** and **Maven**:

```bash
mvn clean package
```

The compiled `.jar` file will be generated in the `target/` directory.
