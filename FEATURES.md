# Overpowered Mobs — Full Feature Documentation

## Mob Boosting

Every hostile mob ( `MobCategory.MONSTER` ) has its attributes multiplied on spawn via a mixin at `Mob.finalizeSpawn` RETURN.

### Boosted attributes

| Attribute         | Default multiplier | Config key            |
|-------------------|-------------------|-----------------------|
| Max Health        | 2.0×              | `healthMultiplier`    |
| Attack Damage     | 2.0×              | `damageMultiplier`    |
| Movement Speed    | 1.5×              | `speedMultiplier`     |
| Armor             | 2.0×              | `armorMultiplier`     |
| Follow Range      | 2.0×              | `followRangeMultiplier`|
| Experience        | 3.0×              | `xpMultiplier`        |
| Drops             | 2.0×              | `dropsMultiplier`     |

### Implementation

- **`OverpoweredConfig.java`** — Loads/saves `config/overpoweredmobs.json` with a `defaults` map and per-mob overrides.
- **`MobAttributesMixin.java`** — Injects into `Mob.finalizeSpawn` to read config and multiply attributes via `AttributeInstance.setBaseValue()`.
- **`ExperienceMultiplierMixin.java`** — Injects at RETURN of `LivingEntity.getExperienceReward()` to multiply XP drops.
- **`DropMultiplierMixin.java`** — Injects after `dropAllDeathLoot()` to duplicate nearby `ItemEntity` instances for multiplied drops.
- Entities are tagged with `opm_boosted` (scoreboard entity tag) to prevent re-boosting.

---

## OP Enchanted Gear

All hostile mobs (except a defined set of non-equippable types) spawn with full OP gear **on the next server tick** after `finalizeSpawn` completes, ensuring nothing overwrites the equipment.

### Armor

Every equippable mob gets full netherite armor:

| Slot      | Item                | Enchantment    | Level |
|-----------|---------------------|----------------|-------|
| Head      | Netherite Helmet    | Protection     | X     |
| Chest     | Netherite Chestplate| Protection     | X     |
| Legs      | Netherite Leggings  | Protection     | X     |
| Feet      | Netherite Boots     | Protection     | X     |

All armor pieces have `setGuaranteedDrop()` enabled.

### Weapons

Weapon selection is based on entity type:

| Entity type                              | Weapon                         | Enchantments                           |
|------------------------------------------|--------------------------------|----------------------------------------|
| Skeleton, Stray, Bogged                  | Bow                            | Power X, Punch III, Flame I            |
| All other equippable hostile mobs        | Netherite Sword                | Sharpness X, Fire Aspect III           |

### Non-equippable mobs

The following mobs are skipped (they don't equip gear):

Creeper, Spider, Cave Spider, Slime, Magma Cube, Enderman, Silverfish, Endermite, Blaze, Ghast, Guardian, Elder Guardian, Witch, Phantom

### Implementation

- **`EquipmentHelper.java`** — Static utility that builds enchanted item stacks using the MC 26.1 data component API (`ItemEnchantments.Mutable` + `DataComponents.ENCHANTMENTS`) and calls `Mob.setItemSlot()` + `Mob.setGuaranteedDrop()`.
- **When gear is applied** — The gear call is deferred via `MinecraftServer.execute()` to run after the entity is fully loaded into the world, preventing native equipment code from overwriting it.

---

## Charged Creepers

All creepers spawn visually charged (`setPowered(true)`) with the lightning bolt aura and doubled explosion radius (6.0 vs normal 3.0).

### Implementation

- **`CreeperHelper.java`** — Uses reflection to access the private `Creeper.DATA_IS_POWERED` entity data accessor (no `setPowered()` method exists in MC 26.1).
- Called from `MobAttributesMixin.onFinalizeSpawn` for every `Creeper` instance.

---

## Commands

All commands are registered under the `/opm` root:

| Command                                     | Permission | Description                         |
|---------------------------------------------|------------|-------------------------------------|
| `/opm status`                               | `2` (op)   | Show all current multipliers        |
| `/opm set <mob> <attr> <value>`             | `2` (op)   | Set a multiplier for a mob type     |
| `/opm reload`                               | `2` (op)   | Reload config from disk             |
| `/opm reset`                                | `2` (op)   | Reset config to default values      |

### Command examples

```
/opm set minecraft:zombie health 3.0
/opm status
/opm reload
/opm reset
```

### Implementation

- **`OPMCommand.java`** — Registers subcommands using Fabric API's `CommandRegistrationCallback`. Parses mob IDs, attribute names, and double values with proper error handling.

---

## Config File

**Path:** `config/overpoweredmobs.json`

Auto-generated with defaults on first server start. Example:

```json
{
  "defaults": {
    "healthMultiplier": 2.0,
    "damageMultiplier": 2.0,
    "speedMultiplier": 1.5,
    "armorMultiplier": 2.0,
    "followRangeMultiplier": 2.0,
    "xpMultiplier": 3.0,
    "dropsMultiplier": 2.0
  },
  "mobs": {
    "minecraft:zombie": {
      "healthMultiplier": 3.0,
      "damageMultiplier": 2.5
    }
  }
}
```

---

## Debug Logging

A separate log file at `logs/overpoweredmobs.log` records every mob spawn event:

```
[2026-07-12 18:48:08.151] [INFO] finalizeSpawn for entity.minecraft.zombie at BlockPos{x=64, y=14, z=7} reason=NATURAL
[2026-07-12 18:48:08.151] [INFO]   boosting entity.minecraft.zombie health=2.0 damage=2.0 speed=1.5
[2026-07-12 18:48:08.151] [INFO]   -> boosted, health=40.0 maxHealth=40.0
[2026-07-12 18:48:08.153] [INFO]   -> equipping gear (deferred) for entity.minecraft.zombie
[2026-07-12 18:48:08.153] [INFO]   -> equipped OP sword
```

### Implementation

- **`OverpoweredMobsLogger.java`** — Writes timestamped messages to `logs/overpoweredmobs.log` using `java.nio.file.Files.writeString()`. Initialized from `FabricLoader.getInstance().getGameDir()`.

---

## Client Compatibility

All gear uses **only vanilla Minecraft items** (netherite armor, bow, netherite sword) with **vanilla enchantments** applied via the standard data component system. No custom items, no custom registries, no registry sync. Vanilla clients connect without errors.

---

## Version History

| Version | Changes |
|---------|---------|
| 1.0.2   | All creepers spawn charged; removed explosion modifier mixin |
| 1.0.1   | Fixed gear overwrite (deferred tick); added debug logger; fixed enchantment component API; creeper 1% charge (removed in 1.0.2) |
| 1.0.0   | Initial release: mob boosting, OP gear, commands |
