package com.overpoweredmobs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class OverpoweredConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("overpoweredmobs.json");

    private Map<String, MobConfig> mobs = new HashMap<>();
    private MobConfig defaults = new MobConfig();

    public MobConfig getFor(EntityType<?> type) {
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null) {
            MobConfig specific = mobs.get(key.toString());
            if (specific != null) return specific;
        }
        return defaults;
    }

    public MobConfig getDefaults() {
        return defaults;
    }

    public Map<String, MobConfig> getMobs() {
        return mobs;
    }

    public void setFor(EntityType<?> type, MobConfig config) {
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null) {
            mobs.put(key.toString(), config);
        }
    }

    public void setDefault(String attr, double value) {
        defaults.set(attr, value);
    }

    public void reset() {
        mobs.clear();
        defaults = new MobConfig();
    }

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
        private double dropMultiplier = 2.0;

        public double healthMultiplier() { return healthMultiplier; }
        public double damageMultiplier() { return damageMultiplier; }
        public double speedMultiplier() { return speedMultiplier; }
        public double armorMultiplier() { return armorMultiplier; }
        public double followRangeMultiplier() { return followRangeMultiplier; }
        public double xpMultiplier() { return xpMultiplier; }
        public double dropMultiplier() { return dropMultiplier; }

        public void setHealthMultiplier(double v) { healthMultiplier = v; }
        public void setDamageMultiplier(double v) { damageMultiplier = v; }
        public void setSpeedMultiplier(double v) { speedMultiplier = v; }
        public void setArmorMultiplier(double v) { armorMultiplier = v; }
        public void setFollowRangeMultiplier(double v) { followRangeMultiplier = v; }
        public void setXpMultiplier(double v) { xpMultiplier = v; }
        public void setDropMultiplier(double v) { dropMultiplier = v; }

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
}
