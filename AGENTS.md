# Overpowered Mobs — Fabric Mod

## Identity
- **Mod ID**: `overpoweredmobs` · **Package**: `com.overpoweredmobs`
- **MC**: 26.1.2 · **Fabric Loader**: 0.19.3 · **Loom**: 1.17.14 · **Fabric API**: 0.154.2+26.1.2
- **Java**: 25 · **Version**: 1.0.3 (`mod_version` in `gradle.properties`)

## Build & Deploy
```bash
./gradlew build
```
Jar auto-copies to `/Users/evanchubbock/Movies/fabric test server/mods/` via `jar.doLast`. No test task, no test dependencies, no test directory — assume zero tests.

## Critical MC 26.1 Quirks
- **No remapping** (unobfuscated MC). Use `implementation`/`compileOnly`/`api`, NEVER `modImplementation`/`modCompileOnly`/`modApi`.
- `ResourceLocation` → `net.minecraft.resources.Identifier` (`.tryParse()`, `.toString()`)
- `MobSpawnType` → `net.minecraft.world.entity.EntitySpawnReason`
- `Attributes.MAX_HEALTH` etc. are `Holder<Attribute>` — pass directly to `LivingEntity.getAttribute(Holder<Attribute>)`
- `Entity.getPersistentData()` removed → use `addTag()` / `entityTags()` (scoreboard entity tags, persist across saves)
- `Registry.get(Identifier)` returns `Optional<Reference<T>>` — use `Registry.getValue(Identifier)` for direct `T`
- Enchantments via data components: `ItemEnchantments.Mutable` + `DataComponents.ENCHANTMENTS`
- No `setPowered(boolean)` on `Creeper` — must use `CreeperHelper` (reflection via `DATA_IS_POWERED`)
- Zombie → `net.minecraft.world.entity.monster.zombie.Zombie`, Skeleton → `.monster.skeleton.*`

## Architecture
- Entry: `OverpoweredMobs.onInitialize()` → init logger, load config, register `/opm` commands, register `ServerLivingEntityEvents.AFTER_DEATH` (zombie piñata handler)
- Boosting/gear/cavalry: `MobAttributesMixin` injects at `Mob.finalizeSpawn` RETURN — only `MobCategory.MONSTER`
- **Gear applied deferred**: `level.getServer().execute(() -> EquipmentHelper.equipOPGear(...))` to prevent subclass overwriting
- Creeper charging: `CreeperHelper.setPowered()` via reflection on private `DATA_IS_POWERED`
- Tags: `opm_boosted` (prevent re-boost), `opm_piñata` (prevent chain), `opm_cavalry_mount` (prevent recursive cavalry)
- Entity lookup: `BuiltInRegistries.ENTITY_TYPE.getKey(type)` / `BuiltInRegistries.ENTITY_TYPE.getValue(Identifier)`
- Config: `config/overpoweredmobs.json` auto-generated; `zombiePiñataChance` default **0.01**, `zombiePiñataCount` default 2
- `EquipmentHelper.NO_EQUIP_TYPES` currently includes `EntityType.ZOMBIE` (TEMP for piñata testing)
- `/opm set <mob> <attr> <value>` — value clamped to [0.1, 100.0]; attrs: `health`, `damage`, `speed`, `armor`, `followRange`, `xp`, `drops`
- Debug log: `logs/overpoweredmobs.log` via `OverpoweredMobsLogger` (also has `warn()`/`error()` methods)
- OP gear uses `setDropChance(slot, 0.0f)` — never drops from mobs
- Piñata babies despawn after 30s via `PinataDespawnMixin` (Mob.tick HEAD, checks `tickCount > 600`)
- Piñata only triggers on player kills; 10+ zombies nearby → flat 75% chance; >1 player nearby → 3 babies instead of 2
- Four mixins: `MobAttributesMixin` (Mob), `ExperienceMultiplierMixin` (LivingEntity), `DropMultiplierMixin` (LivingEntity), `PinataDespawnMixin` (Mob) — all in `com.overpoweredmobs.mixin`

## Code Style
- No records — plain classes with explicit getters
- No generated code, no annotations processing, no migrations
- Vanilla items + vanilla enchantments only (full client compatibility)
