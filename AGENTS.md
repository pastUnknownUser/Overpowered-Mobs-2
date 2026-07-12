# Overpowered Mobs — Fabric Mod

## Identity
- **Mod ID**: `overpoweredmobs` · **Package**: `com.overpoweredmobs`
- **MC**: 26.1.2 · **Fabric Loader**: 0.19.3 · **Loom**: 1.17.14 · **Fabric API**: 0.154.2+26.1.2
- **Java**: 25 · **Build**: `./gradlew build` → `build/libs/overpoweredmobs-<version>.jar`

## Key setup facts
- **MC 26.1+ is unobfuscated** — no mappings, no remapping.
- Use plugin ID `net.fabricmc.fabric-loom` (the LoomNoRemap variant), NOT the old `fabric-loom`.
- No `mappings` dependency. Use `implementation` / `compileOnly` / `api` — NOT `modImplementation` / `modCompileOnly` / `modApi`.
- No mixin config needed unless you're actually using mixins (unobfuscated code means you can use standard Java inheritance/AOP instead).

## Planned features
1. **Boosted vanilla mobs** — multiply HP/damage/speed/armor/drops/XP/AI via mixin or direct override + per-mob-type JSON config
2. **Overpowered gear** — OP Sword (area damage), OP Bow (multishot, explosive arrows), OP Armor (flight), OP Tools (5×5, instant break), custom creative tab
3. **Commands** — `/opm` suite: `set`, `reload`, `status`, `reset`

## State
- **Phase 1 (done)**: Build scripts, mod skeleton, verified `./gradlew build` passes.
- **Phase 2** (next): Mob boosting via config + commands.
- **Phase 3**: All overpowered items + creative tab.
