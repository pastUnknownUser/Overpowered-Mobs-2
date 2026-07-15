package com.overpoweredmobs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.overpoweredmobs.OverpoweredMobs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverpoweredConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("overpoweredmobs.json");

    private boolean enableGear = true;
    private boolean enableCavalry = true;
    private boolean enablePinata = true;
    private boolean testMode = false;
    private boolean enableBossBar = true;
    private double bossBarRange = 32.0;
    private boolean enableMobNames = true;
    private boolean enableAlertSound = true;
    private double chargedCreeperChance = 1.0;
    private boolean enableAggro = true;
    private double aggroFollowRange = 128.0;
    private double aggroSpeedMultiplier = 1.75;
    private double aggroCloseSpeedMultiplier = 2.25;
    private double aggroSlowRange = 10.0;
    private double rangedAttackSpeedMultiplier = 2.0;
    private double ghastExplosionMultiplier = 3.0;
    private double piglinBruteGearChance = 0.5;
    private double silverfishSpeedMultiplier = 2.0;
    private double shulkerLevitationDurationMultiplier = 2.0;
    private boolean enableEvilBunnies = true;
    private boolean enablePiglinHive = true;
    private double piglinHiveChance = 0.1;
    private double piglinHiveRange = 32.0;
    private boolean enableStrongholdMobs = true;
    private int strongholdMobCount = 8;
    private boolean enableAngryWolves = true;
    private boolean enableWaterEndermen = true;
    private Map<String, Double> dimensions = new HashMap<>();
    private Map<String, MobConfig> mobs = new HashMap<>(defaultMobOverrides());
    private MobConfig defaults = new MobConfig();
    private List<CavalryEntry> cavalry = List.of(
        new CavalryEntry("minecraft:zombie", "minecraft:chicken", 0.15, true),
        new CavalryEntry("minecraft:creeper", "minecraft:phantom", 0.03, false),
        new CavalryEntry("minecraft:wither_skeleton", "minecraft:ghast", 0.03, false),
        new CavalryEntry("minecraft:skeleton", "minecraft:skeleton_horse", 0.2, false),
        new CavalryEntry("minecraft:stray", "minecraft:skeleton_horse", 0.2, false),
        new CavalryEntry("minecraft:bogged", "minecraft:skeleton_horse", 0.2, false),
        new CavalryEntry("minecraft:parched", "minecraft:skeleton_horse", 0.2, false)
    );
    private double spawnChance = 0.05;
    private double hordeSpeedMultiplier = 1.3;
    private double hordeFollowRangeMultiplier = 3.0;
    @SerializedName("zombiePi\u00F1ataChance")
    private double zombiePinataChance = 0.01;

    @SerializedName("zombiePi\u00F1ataCount")
    private int zombiePinataCount = 2;

    private static Map<String, MobConfig> defaultMobOverrides() {
        Map<String, MobConfig> map = new HashMap<>();
        MobConfig drowned = new MobConfig();
        drowned.weapon = "minecraft:trident";
        drowned.weaponEnchantments = new HashMap<>(Map.of("minecraft:impaling", 10));
        map.put("minecraft:drowned", drowned);

        MobConfig pillager = new MobConfig();
        pillager.spawnChance = 0.15;
        map.put("minecraft:pillager", pillager);

        MobConfig creeper = new MobConfig();
        creeper.spawnChance = 0.2;
        map.put("minecraft:creeper", creeper);

        return map;
    }

    public boolean isEnableGear() { return enableGear; }
    public boolean isEnableCavalry() { return enableCavalry; }
    public boolean isEnablePinata() { return enablePinata; }
    public boolean isTestMode() { return testMode; }
    public void setTestMode(boolean testMode) { this.testMode = testMode; }
    public boolean isEnableBossBar() { return enableBossBar; }
    public double getBossBarRange() { return bossBarRange; }
    public boolean isEnableMobNames() { return enableMobNames; }
    public boolean isEnableAlertSound() { return enableAlertSound; }
    public double getChargedCreeperChance() { return chargedCreeperChance; }
    public boolean isEnableAggro() { return enableAggro; }
    public double getAggroFollowRange() { return aggroFollowRange; }
    public double getAggroSpeedMultiplier() { return aggroSpeedMultiplier; }
    public double getAggroCloseSpeedMultiplier() { return aggroCloseSpeedMultiplier; }
    public double getAggroSlowRange() { return aggroSlowRange; }
    public double getRangedAttackSpeedMultiplier() { return rangedAttackSpeedMultiplier; }
    public double getGhastExplosionMultiplier() { return ghastExplosionMultiplier; }
    public double getPiglinBruteGearChance() { return piglinBruteGearChance; }
    public double getSilverfishSpeedMultiplier() { return silverfishSpeedMultiplier; }
    public double getShulkerLevitationDurationMultiplier() { return shulkerLevitationDurationMultiplier; }
    public boolean isEnableEvilBunnies() { return enableEvilBunnies; }
    public boolean isEnablePiglinHive() { return enablePiglinHive; }
    public double getPiglinHiveChance() { return piglinHiveChance; }
    public double getPiglinHiveRange() { return piglinHiveRange; }
    public boolean isEnableStrongholdMobs() { return enableStrongholdMobs; }
    public int getStrongholdMobCount() { return strongholdMobCount; }
    public boolean isEnableAngryWolves() { return enableAngryWolves; }
    public boolean isEnableWaterEndermen() { return enableWaterEndermen; }
    public double getDimensionMultiplier(String dimensionId) { return dimensions.getOrDefault(dimensionId, 1.0); }

    public MobConfig getFor(EntityType<?> type) {
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null) {
            MobConfig specific = mobs.get(key.toString());
            if (specific != null) return specific;
        }
        return defaults;
    }

    public double getSpawnChanceFor(EntityType<?> type) {
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null) {
            MobConfig specific = mobs.get(key.toString());
            if (specific != null && specific.spawnChance >= 0) return specific.spawnChance;
        }
        return spawnChance;
    }

    public MobConfig getDefaults() { return defaults; }
    public Map<String, MobConfig> getMobs() { return mobs; }
    public List<CavalryEntry> getCavalry() { return cavalry; }
    public double getSpawnChance() { return spawnChance; }
    public double getHordeSpeedMultiplier() { return hordeSpeedMultiplier; }
    public double getHordeFollowRangeMultiplier() { return hordeFollowRangeMultiplier; }
    public double getZombiePinataChance() { return zombiePinataChance; }
    public int getZombiePinataCount() { return zombiePinataCount; }

    public void setFor(EntityType<?> type, MobConfig config) {
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null) {
            mobs.put(key.toString(), config);
        }
    }

    public void setDefault(String attr, double value) {
        defaults.set(attr, value);
    }

    public static void reset() {
        try {
            Files.deleteIfExists(CONFIG_PATH);
        } catch (IOException e) {
            OverpoweredMobs.LOGGER.error("Failed to delete config", e);
        }
        OverpoweredMobs.loadConfig();
    }

    public static OverpoweredConfig load() {
        if (CONFIG_PATH.toFile().exists()) {
            try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
                Type type = new TypeToken<OverpoweredConfig>(){}.getType();
                OverpoweredConfig config = GSON.fromJson(reader, type);
                if (config != null) {
                    config.defaults.clamp();
                    for (MobConfig mc : config.mobs.values()) {
                        mc.clamp();
                    }
                    return config;
                }
            } catch (IOException e) {
                OverpoweredMobs.LOGGER.error("Failed to load config", e);
            }
        }
        OverpoweredConfig config = new OverpoweredConfig();
        config.save();
        return config;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(this, writer);
            } catch (IOException e) {
                OverpoweredMobs.LOGGER.error("Failed to save config", e);
            }
    }

    public static class MobConfig {
        private double healthMultiplier = 2.0;
        private double damageMultiplier = 2.0;
        private double speedMultiplier = 1.5;
        private double armorMultiplier = 2.0;
        private double followRangeMultiplier = 2.0;
        private double xpMultiplier = 3.0;
        @SerializedName("dropsMultiplier")
        private double dropMultiplier = 2.0;
        private double spawnChance = -1.0;
        private String weapon;
        private Map<String, Integer> weaponEnchantments;

        public double healthMultiplier() { return healthMultiplier; }
        public double damageMultiplier() { return damageMultiplier; }
        public double speedMultiplier() { return speedMultiplier; }
        public double armorMultiplier() { return armorMultiplier; }
        public double followRangeMultiplier() { return followRangeMultiplier; }
        public double xpMultiplier() { return xpMultiplier; }
        public double dropMultiplier() { return dropMultiplier; }
        public String weapon() { return weapon; }
        public Map<String, Integer> weaponEnchantments() { return weaponEnchantments; }

        public void setHealthMultiplier(double v) { healthMultiplier = v; }
        public void setDamageMultiplier(double v) { damageMultiplier = v; }
        public void setSpeedMultiplier(double v) { speedMultiplier = v; }
        public void setArmorMultiplier(double v) { armorMultiplier = v; }
        public void setFollowRangeMultiplier(double v) { followRangeMultiplier = v; }
        public void setXpMultiplier(double v) { xpMultiplier = v; }
        public void setDropMultiplier(double v) { dropMultiplier = v; }

        public void clamp() {
            healthMultiplier = clamp(healthMultiplier);
            damageMultiplier = clamp(damageMultiplier);
            speedMultiplier = clamp(speedMultiplier);
            armorMultiplier = clamp(armorMultiplier);
            followRangeMultiplier = clamp(followRangeMultiplier);
            xpMultiplier = clamp(xpMultiplier);
            dropMultiplier = clamp(dropMultiplier);
        }

        private static double clamp(double v) {
            return Math.max(0.1, Math.min(100.0, v));
        }

        public void set(String attr, double value) {
            switch (attr) {
                case "health" -> healthMultiplier = value;
                case "damage" -> damageMultiplier = value;
                case "speed" -> speedMultiplier = value;
                case "armor" -> armorMultiplier = value;
                case "followRange" -> followRangeMultiplier = value;
                case "xp" -> xpMultiplier = value;
                case "drops" -> dropMultiplier = value;
                case "spawnchance" -> spawnChance = value;
            }
        }

        public double get(String attr) {
            return switch (attr) {
                case "health" -> healthMultiplier;
                case "damage" -> damageMultiplier;
                case "speed" -> speedMultiplier;
                case "armor" -> armorMultiplier;
                case "followRange" -> followRangeMultiplier;
                case "xp" -> xpMultiplier;
                case "drops" -> dropMultiplier;
                case "spawnchance" -> spawnChance;
                default -> 1.0;
            };
        }
    }

    public static class CavalryEntry {
        private String rider;
        private String mount;
        private double chance;
        private boolean baby;

        public CavalryEntry() {}

        public CavalryEntry(String rider, String mount, double chance, boolean baby) {
            this.rider = rider;
            this.mount = mount;
            this.chance = chance;
            this.baby = baby;
        }

        public String rider() { return rider; }
        public String mount() { return mount; }
        public double chance() { return chance; }
        public boolean baby() { return baby; }
    }
}
