# Overpowered Mobs — Fabric Mod

## Identity
- **Mod ID**: `overpoweredmobs` · **Package**: `com.overpoweredmobs`
- **MC**: 26.1.2 · **Fabric Loader**: 0.19.3 · **Loom**: 1.17.14 · **Fabric API**: 0.154.2+26.1.2
- **Java**: 25 · **Build**: `./gradlew build` → auto-deploys to `/Users/evanchubbuck/Movies/fabric test server/mods/`
- **Current version**: 1.0.3 (`mod_version` in `gradle.properties`)

## Setup & Build
- **MC 26.1+ is unobfuscated** — no mappings, no remapping. Use `net.fabricmc.fabric-loom` (LoomNoRemap).
- No `mappings`. Use `implementation` / `compileOnly` / `api` — NOT `modImplementation` / `modCompileOnly` / `modApi`.
- `build.gradle` has `doLast` on `jar` task to auto-copy to test server's `mods/` dir.
- Mixin config at `overpoweredmobs.mixins.json` referenced in `fabric.mod.json`.

## MC 26.1 API changes (vs 1.21.4)
- `ResourceLocation` → `net.minecraft.resources.Identifier` (`.tryParse()`, `.toString()`)
- `MobSpawnType` → `net.minecraft.world.entity.EntitySpawnReason` (values: `NATURAL`, `SPAWNER`, `STRUCTURE`, `TRIGGERED`, `JOCKEY`, etc.)
- `Attributes.MAX_HEALTH` etc. are `Holder<Attribute>` — pass directly to `LivingEntity.getAttribute(Holder<Attribute>)`
- `Entity.getPersistentData()` removed → use `Entity.addTag()` / `entityTags()` (scoreboard entity tags)
- `Registry.get(Identifier)` returns `Optional<Reference<T>>` — use `Registry.getValue(Identifier)` for direct `T`
- `EntityType.create(Level, EntitySpawnReason)` — standard entity creation
- `EntityType.create(ServerLevel, Consumer<T>, BlockPos, EntitySpawnReason, boolean, boolean)` — full version
- `ServerLevel.explode(Entity, double, double, double, float, Level.ExplosionInteraction)` returns void
- Enchantments use data components: `ItemEnchantments.Mutable` + `DataComponents.ENCHANTMENTS`
- `ServerLivingEntityEvents.AFTER_DEATH` — Fabric event for death callbacks (`ServerLivingEntityEvents$AfterDeath`)
- No `ServerEntityEvents.ENTITY_LOAD` in this Fabric API version
- No `setPowered(boolean)` on `Creeper` — must use `CreeperHelper` (reflection via `DATA_IS_POWERED`)
- Zombie classes moved to `net.minecraft.world.entity.monster.zombie` package (Zombie, Drowned, Husk)
- Skeleton classes moved to `net.minecraft.world.entity.monster.skeleton` package (AbstractSkeleton, Skeleton, Stray, Bogged, WitherSkeleton, Parched)

## Files

### Main
| File | Purpose |
|------|---------|
| `OverpoweredMobs.java` | Entry point: init logger, load config, register commands, register piñata AfterDeath handler |
| `OverpoweredMobsClient.java` | Client entry point (empty) |
| `OverpoweredMobsLogger.java` | Writes timestamped logs to `logs/overpoweredmobs.log` |
| `EquipmentHelper.java` | Builds OP enchanted vanilla items and equips them on mobs |
| `CreeperHelper.java` | Reflection-based access to `Creeper.DATA_IS_POWERED` (no public setter in MC 26.1) |

### Config
| File | Purpose |
|------|---------|
| `config/OverpoweredConfig.java` | JSON load/save at `config/overpoweredmobs.json`; inner classes `MobConfig`, `CavalryEntry` |

### Mixins
| File | Target | Purpose |
|------|--------|---------|
| `mixin/MobAttributesMixin.java` | `Mob` | Inject at `finalizeSpawn` RETURN: boost stats, charge creepers, equip gear (deferred), spawn cavalry |
| `mixin/ExperienceMultiplierMixin.java` | `LivingEntity` | Multiply XP drops at `getExperienceReward` RETURN |
| `mixin/DropMultiplierMixin.java` | `LivingEntity` | Duplicate `ItemEntity` instances after `dropAllDeathLoot` |

### Commands
| File | Purpose |
|------|---------|
| `command/OPMCommand.java` | `/opm set/reload/status/reset` subcommands |

## Features

### 1. Mob Boosting
- All `MobCategory.MONSTER` mobs have stats multiplied on spawn via `MobAttributesMixin.onFinalizeSpawn`
- Configurable per-mob-type in JSON with `defaults` fallback
- Tagged `opm_boosted` to prevent re-boosting (scoreboard entity tag, persists across saves)
- Attributes: MAX_HEALTH, ATTACK_DAMAGE, MOVEMENT_SPEED, ARMOR, FOLLOW_RANGE

### 2. OP Enchanted Gear
- Equipped via `EquipmentHelper.equipOPGear()` using vanilla items + OP enchantments
- Applied **deferred** via `MinecraftServer.execute()` (next tick) to prevent subclass overwriting
- Uses `ItemEnchantments.Mutable` + `DataComponents.ENCHANTMENTS` for MC 26.1 compatibility
- `NO_EQUIP_TYPES` set skips non-armorable mobs (spiders, creepers, slimes, etc.)
- Weapon selection by entity type: Skeletons/Stray/Bogged → Power X Bow; else → Sharpness X Netherite Sword
- Armor: Protection X on full Netherite set

### 3. Charged Creepers
- All creepers spawn visually charged via `CreeperHelper.setPowered()` (private field access via reflection)
- `Creeper.DATA_IS_POWERED` accessed through reflection in `CreeperHelper` (no `setPowered()` in MC 26.1)
- Doubled explosion radius (6.0) naturally from `setPowered(true)`

### 4. Zombie Piñata
- On zombie death: `ServerLivingEntityEvents.AFTER_DEATH` fires → roll `zombiePiñataChance` (default 0.001)
- Spawns `zombiePiñataCount` (default 3) baby zombies scattered ±2.5 blocks from corpse
- Tagged `opm_piñata` to prevent chain reactions
- Config keys: `zombiePiñataChance`, `zombiePiñataCount`

### 5. Cavalry Mounts
- When a matching rider mob spawns, roll per-combo chance → spawn mount at same position → `startRiding(mount)`
- Mount gets `finalizeSpawn` called (gets boosted stats + gear through normal flow)
- Mount tagged `opm_cavalry_mount` to prevent recursive cavalry spawning
- Default config entries (configurable in JSON `cavalry` array):
  - `minecraft:zombie` → `minecraft:chicken` at 15% (baby: true)
  - `minecraft:creeper` → `minecraft:phantom` at 3%
  - `minecraft:wither_skeleton` → `minecraft:ghast` at 3%

### 6. Config (`config/overpoweredmobs.json`)
Auto-generated on first launch. Structure:
```json
{
  "defaults": { "healthMultiplier": 2.0, "damageMultiplier": 2.0, ... },
  "mobs": { "minecraft:zombie": { "healthMultiplier": 3.0 } },
  "cavalry": [ { "rider": "...", "mount": "...", "chance": 0.15, "baby": true } ],
  "zombiePiñataChance": 0.001,
  "zombiePiñataCount": 3
}
```

### 7. Commands
`/opm set <mob> <attr> <value>` — `/opm reload` — `/opm status` — `/opm reset`

### 8. Debug Logging
Separate file: `logs/overpoweredmobs.log` — every spawn event, boost values, gear equip, cavalry, piñata.

## Mixin Config (`overpoweredmobs.mixins.json`)
```json
{
    "required": true,
    "package": "com.overpoweredmobs.mixin",
    "compatibilityLevel": "JAVA_21",
    "mixins": [
        "MobAttributesMixin",
        "ExperienceMultiplierMixin",
        "DropMultiplierMixin"
    ],
    "injectors": { "defaultRequire": 1 }
}
```

## Key Patterns

### Accessing private entity data (e.g. Creeper.DATA_IS_POWERED)
```java
// CreeperHelper.java — plain class, NOT a mixin
Field field = Creeper.class.getDeclaredField("DATA_IS_POWERED");
field.setAccessible(true);
EntityDataAccessor<Boolean> DATA_IS_POWERED = (EntityDataAccessor<Boolean>) field.get(null);
creeper.getEntityData().set(DATA_IS_POWERED, true);
```

### Deferred gear equipping (prevent subclass overwriting)
```java
level.getServer().execute(() -> {
    if (!mob.isAlive()) return;
    EquipmentHelper.equipOPGear(mob, level.registryAccess());
});
```

### Enchanting via data components (MC 26.1)
```java
var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
mutable.set(enchants.getOrThrow(key), level);
stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
```

### Entity creation in MC 26.1
```java
EntityType.ZOMBIE.create(serverLevel, EntitySpawnReason.TRIGGERED);
// or
mountType.create(level, EntitySpawnReason.JOCKEY);
```

### Cavalry mounting
```java
mount.setPos(rider.getX(), rider.getY(), rider.getZ());
mount.finalizeSpawn(level, difficulty, reason, null);
level.addFreshEntity(mount);
rider.startRiding(mount);
```
