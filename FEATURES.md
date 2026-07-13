# Overpowered Mobs — Full Feature Documentation

## Project Overview

- **Mod ID**: `overpoweredmobs`
- **Package**: `com.overpoweredmobs`
- **Minecraft**: 26.1.2
- **Fabric Loader**: 0.19.3
- **Fabric Loom**: 1.17.14
- **Fabric API**: 0.154.2+26.1.2
- **Java**: 25
- **Current version**: 1.0.3
- **License**: MIT
- **Environment**: `*` (server + client)

### Build System

- Gradle build via `./gradlew build`
- **No test task, no test dependencies, no test directory** — zero tests
- Jar auto-copies to `/Users/evanchubbock/Movies/fabric test server/mods/` via `jar.doLast` in `build.gradle`
- Uses `net.fabricmc.fabric-loom` (LoomNoRemap) — **no remapping, no mappings, unobfuscated MC**
- Dependencies use `implementation` / `compileOnly` / `api` — NEVER `modImplementation` / `modCompileOnly` / `modApi`
- Sources jar produced via `withSourcesJar()`
- Publishing: `maven-publish` plugin configured for `mavenLocal`

### Dependencies

- Minecraft `com.mojang:minecraft:26.1.2`
- Fabric Loader `net.fabricmc:fabric-loader:0.19.3`
- Fabric API `net.fabricmc.fabric-api:fabric-api:0.154.2+26.1.2`

### Gradle Properties

```
org.gradle.jvmargs=-Xmx4G -XX:+UseG1GC
org.gradle.daemon=true
org.gradle.parallel=true
```

### Mod File Structure

```
src/main/java/com/overpoweredmobs/
├── OverpoweredMobs.java           — Main entry point (ModInitializer)
├── OverpoweredMobsClient.java     — Client entry point (empty, logs "initialized")
├── OverpoweredMobsLogger.java     — Separate log file writer
├── EquipmentHelper.java           — Builds OP gear item stacks
├── CreeperHelper.java             — Reflection-based creeper charging
├── command/
│   └── OPMCommand.java            — /opm command registration
├── config/
│   └── OverpoweredConfig.java     — JSON config load/save
└── mixin/
    ├── MobAttributesMixin.java     — Boosts, gears, charges creepers, spawns cavalry
    ├── ExperienceMultiplierMixin.java — Multiplies XP drops
    ├── DropMultiplierMixin.java    — Multiplies natural item drops
    └── PinataDespawnMixin.java     — Despawns piñata babies after 30s
```

### Resources

```
src/main/resources/
├── fabric.mod.json               — Mod metadata, entrypoints, mixins, dependencies
├── overpoweredmobs.mixins.json   — Mixin config (4 mixins, JAVA_21 compat, defaultRequire=1)
└── assets/overpoweredmobs/lang/en_us.json  — Empty lang file (no custom items)
```

### Code Conventions

- No Java records — plain classes with explicit getter methods
- No generated code, no annotation processing, no migrations
- No static utility class constructors — all private no-arg constructors (`EquipmentHelper`, `CreeperHelper`, `OverpoweredMobsLogger`)
- Mixins use `@Unique` on helper methods but not consistently on injection methods
- Entity tags (scoreboard tags) instead of NBT/persistent data for MC 26.1 compatibility
- Vanilla items and vanilla enchantments only for full client compatibility

### Mixin Config (`overpoweredmobs.mixins.json`)

| Mixin | Target | Injection point | Purpose |
|-------|--------|----------------|---------|
| `MobAttributesMixin` | `Mob` | `finalizeSpawn` RETURN | Boost stats, charge creepers, equip gear (deferred), spawn cavalry |
| `ExperienceMultiplierMixin` | `LivingEntity` | `getExperienceReward` RETURN | Multiply XP by config multiplier |
| `DropMultiplierMixin` | `LivingEntity` | `dropAllDeathLoot` RETURN | Duplicate natural item drops |
| `PinataDespawnMixin` | `Mob` | `tick` HEAD | Remove piñata babies after 30s |

---

## 1. Mob Boosting

### What it does

Every hostile mob (`MobCategory.MONSTER`) has its stats multiplied when it spawns. The boost happens inside `MobAttributesMixin.onFinalizeSpawn`, which injects at RETURN of `Mob.finalizeSpawn(ServerLevelAccessor, DifficultyInstance, EntitySpawnReason, SpawnGroupData)`.

### Boosted attributes

| Attribute | Config key | Default multiplier | Method |
|-----------|-----------|-------------------|--------|
| Max Health | `healthMultiplier` | 2.0× | `Attributes.MAX_HEALTH` — `instance.setBaseValue(base * mult)` |
| Attack Damage | `damageMultiplier` | 2.0× | `Attributes.ATTACK_DAMAGE` |
| Movement Speed | `speedMultiplier` | 1.5× | `Attributes.MOVEMENT_SPEED` |
| Armor | `armorMultiplier` | 2.0× | `Attributes.ARMOR` |
| Follow Range | `followRangeMultiplier` | 2.0× | `Attributes.FOLLOW_RANGE` |
| Experience ¹ | `xpMultiplier` | 3.0× | `ExperienceMultiplierMixin` — `Math.ceil(xp * mult)` |
| Drops ¹ | `dropsMultiplier` | 2.0× | `DropMultiplierMixin` — floor-based item duplication |

¹ Not applied via attributes — separate mixins handle XP and drops.

### How boosting works

1. `MobAttributesMixin.onFinalizeSpawn` fires when any mob calls `finalizeSpawn`
2. Filters to `MobCategory.MONSTER` only — peaceful mobs are skipped
3. Calls `OverpoweredMobs.applyBoosts(mob)`:
   - Checks for `opm_boosted` scoreboard tag — if present, skips (prevents re-boosting)
   - Looks up config via `OverpoweredConfig.getFor(type)` — returns per-mob override if exists, otherwise defaults
   - Multiplies each attribute using `AttributeInstance.setBaseValue(base * multiplier)`
   - Skips multiplier if value is exactly 1.0 (no-op optimization)
   - Sets `mob.setHealth(mob.getMaxHealth())` — full heal after boosting (important for mobs that finalizeSpawn with partial health)
   - Applies `mob.addTag("opm_boosted")` — persists across saves as scoreboard entity tag

### Attribute multiplier details

```java
// OverpoweredMobs.multiplyAttribute()
if (multiplier == 1.0) return;
var instance = mob.getAttribute(attribute);
if (instance != null) {
    instance.setBaseValue(instance.getBaseValue() * multiplier);
}
```

In MC 26.1, `Attributes.MAX_HEALTH` etc. are `Holder<Attribute>` — passed directly to `LivingEntity.getAttribute(Holder<Attribute>)` (not `ResourceKey`).

### Config lookup

```java
// OverpoweredConfig.getFor(EntityType<?> type)
Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
if (key != null) {
    MobConfig specific = mobs.get(key.toString());
    if (specific != null) return specific;
}
return defaults;
```

- Entity type resolved via `BuiltInRegistries.ENTITY_TYPE.getKey(type)` which returns an `Identifier`
- Looked up by string key (e.g. `"minecraft:zombie"`) in the `mobs` map
- If no per-mob override exists, falls back to `defaults`

### Entity tag: `opm_boosted`

- Applied after boosting to prevent re-boosting on subsequent `finalizeSpawn` calls
- Implemented as a scoreboard entity tag (`Entity.addTag()` / `Entity.entityTags()`)
- Persists across saves (unlike NBT which was removed from Entity in MC 26.1)
- Checked at the start of `applyBoosts()` before any attribute modification

---

## 2. OP Enchanted Gear

### What it does

Every equippable hostile mob gets full netherite armor with Protection X and an OP weapon — **applied on the next server tick** after `finalizeSpawn` to prevent overwriting by subclass equipment code.

### Scope

- Only applied to `MobCategory.MONSTER` entities (filtered in `MobAttributesMixin`)
- Excluded via `EquipmentHelper.NO_EQUIP_TYPES` for mobs without equip slots

### Non-equippable mobs (NO_EQUIP_TYPES)

```java
Set.of(
    EntityType.CREEPER,       // No arms
    EntityType.SPIDER,        // No equipment slots
    EntityType.CAVE_SPIDER,   // No equipment slots
    EntityType.SLIME,         // No equipment slots
    EntityType.MAGMA_CUBE,    // No equipment slots
    EntityType.ENDERMAN,      // No equipment slots (except held block)
    EntityType.SILVERFISH,   // No equipment slots
    EntityType.ENDERMITE,     // No equipment slots
    EntityType.BLAZE,         // No equipment slots
    EntityType.GHAST,         // No equipment slots
    EntityType.GUARDIAN,      // No equipment slots
    EntityType.ELDER_GUARDIAN,// No equipment slots
    EntityType.WITCH,         // No equipment slots
    EntityType.PHANTOM        // No equipment slots
);
```

Note: Zombie was temporarily in this set for piñata testing — no longer excluded.

### Armor

| Slot | Item | Enchantment | Level |
|------|------|------------|-------|
| HEAD | Netherite Helmet | Protection | X |
| CHEST | Netherite Chestplate | Protection | X |
| LEGS | Netherite Leggings | Protection | X |
| FEET | Netherite Boots | Protection | X |

All armor pieces use `mob.setDropChance(slot, 0.0f)` — **gear never drops from mobs**. Previously used `setGuaranteedDrop(slot)` which caused 100% drop rate.

### Weapons

| Entity type | Weapon | Enchantments |
|------------|--------|-------------|
| Skeleton, Stray, Bogged | Bow | Power X, Punch III, Flame I |
| All other equippable mobs | Netherite Sword | Sharpness X, Fire Aspect III |

Ranged mob detection via `isRangedMob()`:
```java
return type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.BOGGED;
```

### Piñata baby exception

Piñata baby zombies (`opm_piñata` tag present) skip armor but **still get the weapon**:
```java
boolean isPinata = mob.entityTags().contains(OverpoweredMobs.PINATA_TAG);
if (!isPinata) {
    setSlot(mob, HEAD, ...);
    setSlot(mob, CHEST, ...);
    setSlot(mob, LEGS, ...);
    setSlot(mob, FEET, ...);
}
// weapon is applied regardless
```

### Deferred gear equipping

Gear is not applied inside `finalizeSpawn` directly. Instead:

```java
// MobAttributesMixin.equipGear()
level.getServer().execute(() -> {
    if (!mob.isAlive()) return;  // Safety check — mob may have died in the same tick
    EquipmentHelper.equipOPGear(mob, level.registryAccess());
});
```

This runs on the **next server tick** after the entity is fully loaded into the world, preventing native equipment code (e.g. Zombie's innate `finalizeSpawn` armor, skeleton bow equip) from overwriting the OP gear.

### Enchantment application (MC 26.1 API)

Uses data components instead of the old `enchant()` method:

```java
ItemStack stack = new ItemStack(item);
var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
mutable.set(enchants.getOrThrow(key), level);
stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
```

Enchantment registry lookup via `registryAccess.lookupOrThrow(Registries.ENCHANTMENT)`.

### Slot application

```java
private static void setSlot(Mob mob, EquipmentSlot slot, ItemStack stack) {
    mob.setItemSlot(slot, stack);
    mob.setDropChance(slot, 0.0f);
}
```

- `mob.setItemSlot(slot, stack)` — equips the item
- `mob.setDropChance(slot, 0.0f)` — zero drop chance

### Client compatibility

All gear uses only vanilla Minecraft items (netherite armor, bow, netherite sword) with vanilla enchantments. No custom items, no custom registries, no registry sync. Vanilla clients connect without errors.

---

## 3. Charged Creepers

### What it does

All creepers spawn visually charged with the lightning bolt aura and doubled explosion radius (6.0 vs normal 3.0).

### Implementation

Uses reflection because MC 26.1 **removed the `setPowered(boolean)` method** from `Creeper`. The `DATA_IS_POWERED` field is a private static `EntityDataAccessor<Boolean>`.

```java
// CreeperHelper.java — static initializer
static {
    try {
        Field field = Creeper.class.getDeclaredField("DATA_IS_POWERED");
        field.setAccessible(true);
        DATA_IS_POWERED = (EntityDataAccessor<Boolean>) field.get(null);
    } catch (Exception e) {
        throw new RuntimeException("Failed to access Creeper.DATA_IS_POWERED", e);
    }
}

public static void setPowered(Creeper creeper) {
    creeper.getEntityData().set(DATA_IS_POWERED, true);
}
```

- Reflection happens once in the static initializer (fail-fast on class load)
- Called from `MobAttributesMixin.onFinalizeSpawn` for every `Creeper` instance
- `setPowered(true)` triggers the vanilla charged behavior (lightning aura + 6.0 explosion radius)

---

## 4. Cavalry Mounts

### What it does

When a matching rider mob spawns, it has a configurable chance to spawn on top of a mount entity. The rider immediately mounts it via `startRiding(mount)`.

### Spawn trigger

- Cavalry check runs inside `MobAttributesMixin.trySpawnCavalry()` at the end of `finalizeSpawn`
- Only runs if the level is a `ServerLevel`
- Iterates through `OverpoweredConfig.getCavalry()` — a list of `CavalryEntry` objects

### Cavalry entry structure

```java
public static class CavalryEntry {
    private String rider;    // e.g. "minecraft:zombie"
    private String mount;    // e.g. "minecraft:chicken"
    private double chance;   // e.g. 0.15 (15%)
    private boolean baby;    // if true AND rider is Zombie, set rider as baby
}
```

### Default combos

| Rider | Mount | Chance | Baby rider |
|-------|-------|--------|------------|
| `minecraft:zombie` | `minecraft:chicken` | 15% | Yes — zombie spawns as baby |
| `minecraft:creeper` | `minecraft:phantom` | 3% | No |
| `minecraft:wither_skeleton` | `minecraft:ghast` | 3% | No |

### Cavalry flow

```
1. Match rider type → iterate cavalry entries → check rider.equals(entry.rider())
2. Roll random: if nextDouble() >= entry.chance() → skip (entry.chance() = probability of mount spawning)
3. Look up mount entity type via BuiltInRegistries.ENTITY_TYPE.getValue(Identifier)
4. Create mount: mountType.create(level, EntitySpawnReason.JOCKEY)
5. Position mount: mount.setPos(rider.getX(), rider.getY(), rider.getZ())
6. finalizeSpawn mount: mount.finalizeSpawn(level, difficulty, JOCKEY, null)
   → Mount flows through MobAttributesMixin normally (gets boosted + geared)
7. Tag mount with "opm_cavalry_mount" to prevent recursive cavalry
8. Add mount to world: level.addFreshEntity(mount)
9. If entry.baby() && rider instanceof Zombie → rider.setBaby(true)
10. Rider mounts: rider.startRiding(mount)
```

### Important details

- Mount uses `EntitySpawnReason.JOCKEY` for both `create()` and `finalizeSpawn()` — fixed from earlier bug where rider's `reason` was used
- Mount gets **full boosting and gear** through the normal `finalizeSpawn` → mixin pipeline
- `opm_cavalry_mount` tag prevents infinite loops (mount won't trigger cavalry for itself)
- If mount lookup fails (`Identifier.tryParse` returns null, or `getValue` returns null), cavalry is silently skipped

---

## 5. Zombie Piñata

### What it does

When a player kills a zombie, there is a configurable chance the zombie explodes into baby zombies.

### Trigger conditions

| Condition | Behavior |
|-----------|----------|
| Entity must be a `Zombie` | `entity instanceof Zombie` |
| Must not already have `opm_piñata` tag | Prevents chain reactions |
| Must be in a `ServerLevel` | `level() instanceof ServerLevel` |
| Killer must be a `ServerPlayer` | `damageSource.getEntity() instanceof ServerPlayer` |
| Base chance roll | `random.nextDouble() >= zombiePiñataChance` (default 0.01 = 1%) |

### Spawn rules

| Condition | Effect |
|-----------|--------|
| Base config | `zombiePiñataChance` (default 0.01 = 1%), `zombiePiñataCount` (default 2) |
| ≥10 zombies within 20 blocks | Chance overridden to flat 0.75 (75%) — anti-XP-farm measure |
| >1 player within 20 blocks | Spawn count increased to 3 instead of default |

### Density check

```java
AABB area = AABB.ofSize(zombie.position(), 40, 40, 40);  // 20 blocks radius
int nearbyZombies = serverLevel.getEntitiesOfClass(Zombie.class, area).size();
if (nearbyZombies >= 10) {
    chance = 0.75;
}
```

- `AABB.ofSize(center, width, height, depth)` — 40×40×40 box centered on corpse
- Counts ALL zombies in that area (including the dying one — still in world when AFTER_DEATH fires)

### Multiplayer check

```java
int nearbyPlayers = 0;
for (ServerPlayer player : serverLevel.players()) {
    if (player.distanceToSqr(zombie) < 400.0) nearbyPlayers++;
}
if (nearbyPlayers > 1) {
    count = 3;
}
```

- `distanceToSqr < 400.0` = within 20 blocks
- Only players on the same dimension count (serverLevel.players() is per-dimension)

### Baby zombie spawning

```java
for (int i = 0; i < count; i++) {
    Zombie baby = EntityType.ZOMBIE.create(serverLevel, EntitySpawnReason.TRIGGERED);
    if (baby == null) continue;

    double ox = (zombie.getRandom().nextDouble() - 0.5) * 5.0;
    double oz = (zombie.getRandom().nextDouble() - 0.5) * 5.0;
    baby.setPos(zombie.getX() + ox, zombie.getY(), zombie.getZ() + oz);
    baby.setBaby(true);
    baby.addTag(PINATA_TAG);
    baby.finalizeSpawn(serverLevel, difficulty, EntitySpawnReason.TRIGGERED, null);
    serverLevel.addFreshEntity(baby);
}
```

- `EntityType.ZOMBIE.create()` with `EntitySpawnReason.TRIGGERED` — returns null if can't spawn
- Scatter: uniform random ±2.5 blocks in X and Z (5.0 * [0, 1) minus 0.5 = [-2.5, 2.5))
- Same Y as dead zombie
- `setBaby(true)` — baby zombie, smaller + faster + burns in daylight
- Tagged `opm_piñata` to prevent chain piñata on death
- `finalizeSpawn(... TRIGGERED)` triggers MobAttributesMixin → baby gets boosted stats + weapon (but no armor, handled in EquipmentHelper)

### Baby zombie behavior

- **Boosted stats** — they flow through `finalizeSpawn` → `MobAttributesMixin` → `applyBoosts()`, so they get the full health/damage/speed multiplier
- **No armor** — `EquipmentHelper.equipOPGear()` checks `opm_piñata` tag and skips armor slots
- **OP weapon** — still get the Netherite Sword (or Bow for ranged mob types, though zombies always get sword)
- **Despawn after 30 seconds** — `PinataDespawnMixin` checks every tick: if `tickCount > 600`, removed via `Entity.RemovalReason.DISCARDED`
- **Cannot trigger another piñata** — `opm_piñata` tag checked in AFTER_DEATH handler

### Despawn timer (PinataDespawnMixin)

```java
@Unique
@Inject(method = "tick", at = @At("HEAD"))
private void onTick(CallbackInfo ci) {
    Mob mob = (Mob) (Object) this;
    if (mob.entityTags().contains(OverpoweredMobs.PINATA_TAG) && mob.tickCount > 600) {
        mob.remove(Entity.RemovalReason.DISCARDED);
    }
}
```

- 600 ticks = 30 seconds (20 ticks/sec)
- Check runs on every tick (HEAD of `Mob.tick()`)
- Uses `Entity.RemovalReason.DISCARDED` — clean removal without death effects or drops

---

## 6. Commands

All commands registered under `/opm` via `CommandRegistrationCallback.EVENT` in `OverpoweredMobs.onInitialize()`.

### Command table

| Command | Permission | Behavior |
|---------|-----------|----------|
| `/opm status` | 2 (op) | Lists default multipliers, then per-mob overrides |
| `/opm set <mob> <attr> <value>` | 2 (op) | Sets an attribute multiplier for a mob type, saves to disk |
| `/opm reload` | 2 (op) | Re-reads config from disk via `OverpoweredMobs.loadConfig()` |
| `/opm reset` | 2 (op) | Clears all per-mob overrides and resets defaults, saves to disk |

### `/opm set` details

- `<mob>` — entity type ID (e.g. `zombie`, `minecraft:zombie`, `skeleton`). If no `:` is present, prepends `minecraft:`
- `<attr>` — one of: `health`, `damage`, `speed`, `armor`, `followRange`, `xp`, `drops`
- `<value>` — double clamped to **[0.1, 100.0]**
- Sets the multiplier on the per-mob override config, then saves the entire config to disk
- Returns error "Unknown entity type: ..." if the mob ID doesn't resolve via `BuiltInRegistries.ENTITY_TYPE`

### `/opm status` output

```
=== Default multipliers ===
  health: 2.0
  damage: 2.0
  speed: 1.5
  armor: 2.0
  followRange: 2.0
  xp: 3.0
  drops: 2.0
=== Per-mob overrides ===
  minecraft:zombie:
    health: 3.0
    damage: 2.5
    speed: 1.0
    ...
```

### `/opm reload`

- Calls `OverpoweredMobs.loadConfig()` which re-reads `config/overpoweredmobs.json` from disk
- If file doesn't exist, regenerates with defaults

### `/opm reset`

- Calls `config.reset()` which clears `mobs` map and resets `defaults` to a new `MobConfig()`
- Saves immediately to disk

### Entity type resolution

```java
private static EntityType<?> findEntityType(String str) {
    if (!str.contains(":")) str = "minecraft:" + str;
    Identifier id = Identifier.tryParse(str);
    if (id == null) return null;
    return BuiltInRegistries.ENTITY_TYPE.getValue(id);
}
```

- Auto-prepends `minecraft:` namespace if missing
- Uses `Identifier.tryParse()` (returns null on invalid format)
- Looks up via `BuiltInRegistries.ENTITY_TYPE.getValue(Identifier)` (returns null if not found)

### Command implementation

- Uses Fabric API's `CommandRegistrationCallback` event
- Registered with `com.mojang.brigadier` CommandDispatcher
- `set` uses `StringArgumentType.word()` for mob and attribute names
- `set` uses `DoubleArgumentType.doubleArg(0.1, 100.0)` for value
- Success messages use `sendSuccess(() -> Component.literal(...), true)` — broadcasts to ops
- Status output uses `sendSuccess(() -> ..., false)` — only visible to command sender

---

## 7. Config File

### Location

`config/overpoweredmobs.json` — resolved via `FabricLoader.getInstance().getConfigDir()`

### Auto-generation

On first launch (or if config file is missing), `OverpoweredConfig.load()` creates a new config with defaults and saves it immediately.

### Serialization

Uses Gson with `setPrettyPrinting()`:
```java
private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
```

### Complete config structure

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

### Config loading

```java
public static OverpoweredConfig load() {
    if (CONFIG_PATH.toFile().exists()) {
        try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
            Type type = new TypeToken<OverpoweredConfig>(){}.getType();
            return GSON.fromJson(reader, type);
        } catch (IOException e) {
            OverpoweredMobs.LOGGER.error("Failed to load config", e);
        }
    }
    OverpoweredConfig config = new OverpoweredConfig();
    config.save();
    return config;
}
```

- On parse error (IOException): logs error, falls through to create defaults
- Otherwise: creates new config with defaults and saves

### MobConfig inner class

Fields and their defaults:
```java
double healthMultiplier = 2.0;
double damageMultiplier = 2.0;
double speedMultiplier = 1.5;
double armorMultiplier = 2.0;
double followRangeMultiplier = 2.0;
double xpMultiplier = 3.0;
double dropMultiplier = 2.0;
```

Config attribute name mapping (used by `/opm set`):
| Command attribute | Config field |
|-----------------|-------------|
| `health` | `healthMultiplier` |
| `damage` | `damageMultiplier` |
| `speed` | `speedMultiplier` |
| `armor` | `armorMultiplier` |
| `followRange` | `followRangeMultiplier` |
| `xp` | `xpMultiplier` |
| `drops` | `dropMultiplier` |

### CavalryEntry inner class

```java
public static class CavalryEntry {
    private String rider;    // Entity type ID string
    private String mount;    // Entity type ID string
    private double chance;   // Probability [0.0, 1.0]
    private boolean baby;    // Make rider a baby (zombie only)
}
```

---

## 8. Debug Logging

### Log file

`logs/overpoweredmobs.log` — separate from Minecraft's main log.

### Log levels

| Method | Prefix | Use |
|--------|--------|-----|
| `info()` | `[INFO]` | Spawn events, boost values, gear equip, cavalry, piñata |
| `warn()` | `[WARN]` | Warnings |
| `error()` | `[ERROR]` | Errors |

### Timestamp format

`yyyy-MM-dd HH:mm:ss.SSS` — e.g. `2026-07-12 18:48:08.151`

### Log example

```
[2026-07-12 18:48:08.151] [INFO] finalizeSpawn for entity.minecraft.zombie at BlockPos{x=64, y=14, z=7} reason=NATURAL
[2026-07-12 18:48:08.151] [INFO]   boosting entity.minecraft.zombie health=2.0 damage=2.0 speed=1.5
[2026-07-12 18:48:08.151] [INFO]   -> boosted, health=40.0 maxHealth=40.0
[2026-07-12 18:48:08.153] [INFO]   -> equipping gear (deferred) for entity.minecraft.zombie
[2026-07-12 18:48:08.153] [INFO]   -> equipped OP sword
```

### Implementation

```java
public final class OverpoweredMobsLogger {
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static Path logFile;

    public static void init(Path gameDir) {
        logFile = gameDir.resolve("logs").resolve("overpoweredmobs.log");
        // Creates parent directory, truncates existing file
    }

    private static void write(String level, String msg) {
        if (logFile == null) return;  // Init failed or couldn't create dir
        String line = String.format("[%s] [%s] %s%n", LocalDateTime.now().format(TIMESTAMP), level, msg);
        Files.writeString(logFile, line, StandardOpenOption.APPEND);
    }
}
```

- Log file is truncated on each server start (`TRUNCATE_EXISTING`)
- If initialization fails (can't create logs dir), `logFile` is set to null and all writes are silently dropped
- Each write operation opens, appends, and closes the file

### What gets logged

- Every `finalizeSpawn` call: entity type, position, spawn reason
- Boost application: attribute multipliers, resulting health
- Gear equipping: deferred equipping start, weapon type (bow/sword)
- Cavalry: rider riding mount message
- Piñata: each kill evaluation, chance values, spawn count, skipped/not-a-zombie messages
- Config loaded on startup

---

## 9. Experience Multiplier

### What it does

Multiplies the XP dropped by mobs when killed.

### Implementation

```java
@Inject(method = "getExperienceReward", at = @At("RETURN"), cancellable = true)
private void multiplyExperience(ServerLevel level, Entity attacker, CallbackInfoReturnable<Integer> cir) {
    if (!(((Object) this) instanceof Mob mob)) return;
    int xp = cir.getReturnValueI();
    if (xp <= 0) return;
    OverpoweredConfig config = OverpoweredMobs.getConfig();
    OverpoweredConfig.MobConfig cfg = config.getFor(mob.getType());
    cir.setReturnValue((int) Math.ceil(xp * cfg.xpMultiplier()));
}
```

- Targets `LivingEntity.getExperienceReward(ServerLevel, Entity)` RETURN
- Only affects `Mob` instances (not players or other living entities)
- Skips if XP is 0 or negative
- Uses `Math.ceil()` for rounding (ensures at least 1 XP if multiplier > 0 and base XP > 0)
- Config lookup via `config.getFor(mob.getType())` — per-mob override or defaults

---

## 10. Drop Multiplier (Natural Drops Only)

### What it does

Multiplies natural item drops (flesh, bones, arrows, etc.) when a mob dies. **Does not affect OP enchanted gear** — those have `setDropChance(slot, 0.0f)`.

### Implementation

```java
@Unique
private static final int DROP_RADIUS = 5;

@Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
private void afterDropAllDeathLoot(ServerLevel level, DamageSource source, CallbackInfo ci) {
    LivingEntity entity = (LivingEntity) (Object) this;
    if (!(entity instanceof Mob mob)) return;

    double multiplier = cfg.dropMultiplier();
    if (multiplier <= 1.0) return;

    // Scan 5-block radius for ItemEntity instances
    for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, entity.getBoundingBox().inflate(DROP_RADIUS))) {
        if (item.isAlive() && item.distanceToSqr(mx, my, mz) < DROP_RADIUS * DROP_RADIUS) {
            // Calculate extra count (e.g. multiplier 2.0 → duplicate 1 extra stack)
            int extraCount = (int) Math.floor(stack.getCount() * (multiplier - 1.0));
            if (extraCount > 0) {
                // Create new item entity with same position and velocity
                ItemEntity extra = new ItemEntity(level, item.getX(), item.getY(), item.getZ(), stack);
                extra.setDeltaMovement(item.getDeltaMovement());
                level.addFreshEntity(extra);
            }
        }
    }
}
```

### Algorithm

1. After `dropAllDeathLoot()` completes, scan for `ItemEntity` within 5 blocks of mob
2. For each item, calculate `extraCount = floor(stackSize * (multiplier - 1.0))`
   - multiplier 2.0 → 1 extra copy (floor(1 * 1.0) = 1)
   - multiplier 3.0 → 2 extra copies (floor(1 * 2.0) = 2)
   - multiplier 1.5 → 0 extra if stack size is 1 (floor(1 * 0.5) = 0), 1 extra if stack size 2 (floor(2 * 0.5) = 1)
3. Create new `ItemEntity` at same position with same velocity

### Scoreboard entity tag check

`entityTags()` is a `Set<String>` — contains check is O(1).

---

## 11. Version History

| Version | Changes |
|---------|---------|
| 1.0.3 | Added cavalry mounts, zombie piñata, debug logger, zero-drop OP gear, piñata despawn timer, density-based and multiplayer piñata rules |
| 1.0.2 | All creepers spawn charged; removed explosion modifier mixin |
| 1.0.1 | Fixed gear overwrite (deferred tick); added debug logger; fixed enchantment component API; creeper 1% charge (removed in 1.0.2) |
| 1.0.0 | Initial release: mob boosting, OP gear, commands |

---

## Footnotes

¹ **Natural drops only** (flesh, bones, arrows, etc.). OP enchanted gear has `setDropChance(slot, 0.0f)` and never drops from mobs.
