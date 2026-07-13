# Overpowered Mobs

A Fabric mod for Minecraft 26.1.2 that turns hostile mobs into formidable enemies with boosted stats, OP enchanted gear, and charged creepers — all using only vanilla items for full vanilla client compatibility.

## Features

- **Mob Boosting** — Multiplied health, damage, speed, armor, follow range, XP, and drops for all hostile mobs
- **OP Enchanted Gear** — Hostile mobs spawn with full netherite armor (Protection X) and OP weapons (Sharpness X / Power X)
- **Charged Creepers** — All creepers spawn visually charged with doubled explosion radius
- **No Mod Required on Client** — Uses only vanilla items with enchantments; vanilla clients connect without issues
- **Fully Configurable** — JSON config with per-mob-type overrides
- **Commands** — `/opm` suite for runtime control

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
| Drops           | 2.0×    |

### Per-mob overrides

```json
{
  "defaults": { "healthMultiplier": 2.0, "damageMultiplier": 2.0, ... },
  "mobs": {
    "minecraft:zombie": { "healthMultiplier": 3.0 },
    "minecraft:skeleton": { "speedMultiplier": 2.0 }
  }
}
```

## Commands

| Command                        | Description                  |
|--------------------------------|------------------------------|
| `/opm status`                  | Show current multipliers     |
| `/opm set <mob> <attr> <val>`  | Set a multiplier             |
| `/opm reload`                  | Reload config from disk      |
| `/opm reset`                   | Reset config to defaults     |

## Build

```bash
./gradlew build
```

Output: `build/libs/overpoweredmobs-<version>.jar`

## Requirements

- Minecraft 26.1.2
- Fabric Loader 0.19.3+
- Fabric API 0.154.2+
- Java 25
