# Overpowered Mobs — Fabric Mod

## Identity
- **Mod ID**: `overpoweredmobs` · **Package**: `com.overpoweredmobs`
- **MC**: 26.1.2 · **Fabric Loader**: 0.19.3 · **Loom**: 1.17.14 · **Fabric API**: 0.154.2+26.1.2
- **Java**: 25 · **Build**: `./gradlew build` → `build/libs/overpoweredmobs-<version>.jar`

## Key setup facts
- **MC 26.1+ is unobfuscated** — no mappings, no remapping.
- Use plugin ID `net.fabricmc.fabric-loom` (the LoomNoRemap variant), NOT the old `fabric-loom`.
- No `mappings` dependency. Use `implementation` / `compileOnly` / `api` — NOT `modImplementation` / `modCompileOnly` / `modApi`.
- Mixin config needed for `@Mixin` classes (`overpoweredmobs.mixins.json`), referenced in `fabric.mod.json`.

## MC 26.1 API changes vs 1.21.4
- `ResourceLocation` → `net.minecraft.resources.Identifier` (still has `.tryParse()`, `.toString()`, etc.)
- `MobSpawnType` → `net.minecraft.world.entity.EntitySpawnReason`
- `Attributes.MAX_HEALTH` etc. are now `Holder<Attribute>`, passed directly to `LivingEntity.getAttribute(Holder<Attribute>)` — no conversion needed
- `Entity.getPersistentData()` removed → use `Entity.addTag()` / `entityTags()` (scoreboard entity tags) for marking
- `Registry.get(Identifier)` returns `Optional<Reference<T>>` — use `Registry.getValue(Identifier)` for direct `T`
- Command registration: `CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, commandSelection) -> ...)` — 3 params

## Planned features
1. **Boosted vanilla mobs** — multiply HP/damage/speed/armor/drops/XP/AI via mixin + per-mob-type JSON config
2. **Overpowered gear** — OP Sword (area damage), OP Bow (multishot, explosive arrows), OP Armor (flight), OP Tools (5×5, instant break), custom creative tab
3. **Commands** — `/opm` suite: `set`, `reload`, `status`, `reset`

## State
- **Phase 1 (done)**: Build scripts, mod skeleton, verified `./gradlew build` passes.
- **Phase 2 (done)**: Mob boosting via `MobAttributesMixin` + `OverpoweredConfig` (JSON at `config/overpoweredmobs.json`) + `/opm` commands (`set`, `reload`, `status`, `reset`).
- **Phase 3** (next): All overpowered items + creative tab.
