# Overpowered Mobs

A Fabric mod for Minecraft 26.1.2+ that turns hostile mobs into formidable enemies with boosted stats, OP enchanted gear, and charged creepers — all using only vanilla items for full vanilla client compatibility.

## Features

- **Mob Boosting** — Multiplied health, damage, speed, armor, follow range, and XP for all hostile mobs
- **Dynamic Drop Multiplier** — 1.2× drops without armor, 3× drops with armor equipped
- **Horde Mode** — Mob that fail the spawn roll get 1.3× speed + 3× follow range
- **Dimension Multipliers** — Per-dimension stat scaling (e.g., nerf overworld, buff nether)
- **OP Enchanted Gear** — Hostile mobs spawn with full netherite armor (Protection X) and OP weapons — gear never drops from mobs
- **Custom Weapon Config** — Per-mob weapon + enchantment overrides (Drowned defaults to Trident + Impaling X)
- **Charged Creepers** — All creepers spawn visually charged with doubled explosion radius
- **Cavalry Mounts** — Hostile mobs can spawn riding mounts (zombie on chicken, creeper on phantom, wither skeleton on ghast)
- **Zombie Piñata** — Zombies explode into baby zombies on death (player kills only, 1% base chance, 75% when densely packed) with firework sound and particle burst
- **Feature Toggles** — `enableGear`, `enableCavalry`, `enablePinata` flags in config
- **No Mod Required on Client** — Uses only vanilla items with enchantments; vanilla clients connect without issues
- **Fully Configurable** — JSON config with per-mob-type overrides
- **Commands** — `/opm` suite for runtime control (operator only)

## Config

`config/overpoweredmobs.json` is auto-generated on first launch.

### Default multipliers

| Attribute       | Default |
|-----------------|---------|
| Health          | 2.0×    |
| Damage          | 2.0×    |
| Speed           | 1.5×    |
| Armor           | 2.0×    |
| Follow Range    | 2.0×    |
| XP              | 3.0×    |

### Full config structure

```json
{
  "enableGear": true,
  "enableCavalry": true,
  "enablePinata": true,
  "defaults": { "healthMultiplier": 2.0, "damageMultiplier": 2.0, ... },
  "mobs": {
    "minecraft:zombie": { "healthMultiplier": 3.0 },
    "minecraft:drowned": {
      "weapon": "minecraft:trident",
      "weaponEnchantments": { "minecraft:impaling": 10 }
    }
  },
  "dimensions": {
    "minecraft:the_nether": 0.5
  },
  "cavalry": [
    { "rider": "minecraft:zombie", "mount": "minecraft:chicken", "chance": 0.15, "baby": true },
    { "rider": "minecraft:creeper", "mount": "minecraft:phantom", "chance": 0.03, "baby": false },
    { "rider": "minecraft:wither_skeleton", "mount": "minecraft:ghast", "chance": 0.03, "baby": false }
  ],
  "zombiePiñataChance": 0.01,
  "zombiePiñataCount": 2
}
```

## Commands

All `/opm` commands require **operator** (moderator+) permission.

| Command                        | Description                  |
|--------------------------------|------------------------------|
| `/opm status`                  | Show current multipliers     |
| `/opm set <mob> <attr> <val>`  | Set a multiplier             |
| `/opm reload`                  | Reload config from disk      |
| `/opm reset`                   | Reset config to defaults     |
| `/opm test`                    | Toggle test mode (100% odds) |

## Build

```bash
./gradlew :26.1.2:build    # build for 26.1.2
./gradlew :26.2:build       # build for 26.2
```

Output: `build/libs/overpoweredmobs-<version>+mc<mc_version>.jar`

## Requirements

- Minecraft 26.1.2 or 26.2
- Fabric Loader 0.19.3+
- Fabric API 0.154.2+
- Java 25
