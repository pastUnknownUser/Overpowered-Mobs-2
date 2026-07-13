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
    private Map<String, Double> dimensions = new HashMap<>();
    private Map<String, MobConfig> mobs = new HashMap<>(defaultMobOverrides());
    private MobConfig defaults = new MobConfig();
    private List<CavalryEntry> cavalry = List.of(
        new CavalryEntry("minecraft:zombie", "minecraft:chicken", 0.15, true),
        new CavalryEntry("minecraft:creeper", "minecraft:phantom", 0.03, false),
        new CavalryEntry("minecraft:wither_skeleton", "minecraft:ghast", 0.03, false)
    );
    private double spawnChance = 0.05;
    private double zombiePiñataChance = 0.01;
    private int zombiePiñataCount = 2;

    private static Map<String, MobConfig> defaultMobOverrides() {
        Map<String, MobConfig> map = new HashMap<>();
        MobConfig drowned = new MobConfig();
        drowned.weapon = "minecraft:trident";
        drowned.weaponEnchantments = new HashMap<>(Map.of("minecraft:impaling", 10));
        map.put("minecraft:drowned", drowned);
        return map;
    }

    public boolean isEnableGear() { return enableGear; }
    public boolean isEnableCavalry() { return enableCavalry; }
    public boolean isEnablePinata() { return enablePinata; }
    public boolean isTestMode() { return testMode; }
    public void setTestMode(boolean testMode) { this.testMode = testMode; }
    public double getDimensionMultiplier(String dimensionId) { return dimensions.getOrDefault(dimensionId, 1.0); }

    public MobConfig getFor(EntityType<?> type) {
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null) {
            MobConfig specific = mobs.get(key.toString());
            if (specific != null) return specific;
        }
        return defaults;
    }

    public MobConfig getDefaults() { return defaults; }
    public Map<String, MobConfig> getMobs() { return mobs; }
    public List<CavalryEntry> getCavalry() { return cavalry; }
    public double getSpawnChance() { return spawnChance; }
    public double getZombiePiñataChance() { return zombiePiñataChance; }
    public int getZombiePiñataCount() { return zombiePiñataCount; }

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
