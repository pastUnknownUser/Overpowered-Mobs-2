package com.overpoweredmobs;

import com.overpoweredmobs.command.OPMCommand;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverpoweredMobs implements ModInitializer {
    public static final String MOD_ID = "overpoweredmobs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String BOOSTED_TAG = "opm_boosted";

    private static OverpoweredConfig config;

    public static OverpoweredConfig getConfig() {
        return config;
    }

    public static void loadConfig() {
        config = OverpoweredConfig.load();
    }

    public static void applyBoosts(Mob mob) {
        if (mob.entityTags().contains(BOOSTED_TAG)) {
            OverpoweredMobsLogger.info("  -> skip boost: already boosted");
            return;
        }

        EntityType<?> type = mob.getType();
        OverpoweredConfig.MobConfig cfg = config.getFor(type);
        OverpoweredMobsLogger.info("  boosting " + type + " health=" + cfg.healthMultiplier() + " damage=" + cfg.damageMultiplier() + " speed=" + cfg.speedMultiplier());

        multiplyAttribute(mob, Attributes.MAX_HEALTH, cfg.healthMultiplier());
        multiplyAttribute(mob, Attributes.ATTACK_DAMAGE, cfg.damageMultiplier());
        multiplyAttribute(mob, Attributes.MOVEMENT_SPEED, cfg.speedMultiplier());
        multiplyAttribute(mob, Attributes.ARMOR, cfg.armorMultiplier());
        multiplyAttribute(mob, Attributes.FOLLOW_RANGE, cfg.followRangeMultiplier());

        mob.setHealth(mob.getMaxHealth());
        mob.addTag(BOOSTED_TAG);
        OverpoweredMobsLogger.info("  -> boosted, health=" + mob.getHealth() + " maxHealth=" + mob.getMaxHealth());
    }

    private static void multiplyAttribute(Mob mob, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double multiplier) {
        if (multiplier == 1.0) return;
        var instance = mob.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() * multiplier);
        }
    }

    @Override
    public void onInitialize() {
        OverpoweredMobsLogger.init(FabricLoader.getInstance().getGameDir());
        loadConfig();

        CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, commandSelection) ->
            OPMCommand.register(dispatcher)
        );

        LOGGER.info("Overpowered Mobs initialized!");
        OverpoweredMobsLogger.info("Overpowered Mobs initialized");
    }
}
