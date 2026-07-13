package com.overpoweredmobs;

import com.overpoweredmobs.command.OPMCommand;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.AABB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverpoweredMobs implements ModInitializer {
    public static final String MOD_ID = "overpoweredmobs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String BOOSTED_TAG = "opm_boosted";
    public static final String PINATA_TAG = "opm_piñata";

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
        double dimMult = config.getDimensionMultiplier(mob.level().dimension().identifier().toString());
        double healthMult = cfg.healthMultiplier() * dimMult;
        double damageMult = cfg.damageMultiplier() * dimMult;
        double speedMult = cfg.speedMultiplier() * dimMult;
        double armorMult = cfg.armorMultiplier() * dimMult;
        double followRangeMult = cfg.followRangeMultiplier() * dimMult;
        OverpoweredMobsLogger.info("  boosting " + type + " health=" + healthMult + " damage=" + damageMult + " speed=" + speedMult + " dim=" + mob.level().dimension().identifier() + " dimMult=" + dimMult);

        multiplyAttribute(mob, Attributes.MAX_HEALTH, healthMult);
        multiplyAttribute(mob, Attributes.ATTACK_DAMAGE, damageMult);
        multiplyAttribute(mob, Attributes.MOVEMENT_SPEED, speedMult);
        multiplyAttribute(mob, Attributes.ARMOR, armorMult);
        multiplyAttribute(mob, Attributes.FOLLOW_RANGE, followRangeMult);

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

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!config.isEnablePinata()) return;
            if (!(entity instanceof Zombie zombie)) return;
            if (zombie.entityTags().contains(PINATA_TAG)) return;
            if (!(zombie.level() instanceof ServerLevel serverLevel)) return;
            if (!(damageSource.getEntity() instanceof ServerPlayer)) return;

            double chance = config.getZombiePiñataChance();

            AABB area = AABB.ofSize(zombie.position(), 40, 40, 40);
            int nearbyZombies = serverLevel.getEntitiesOfClass(Zombie.class, area).size();
            if (nearbyZombies >= 10) {
                chance = 0.75;
            }

            if (zombie.getRandom().nextDouble() >= chance) return;

            int count = config.getZombiePiñataCount();
            int nearbyPlayers = 0;
            for (ServerPlayer player : serverLevel.players()) {
                if (player.distanceToSqr(zombie) < 400.0) nearbyPlayers++;
            }
            if (nearbyPlayers > 1) {
                count = 3;
            }

            DifficultyInstance difficulty = serverLevel.getCurrentDifficultyAt(zombie.blockPosition());
            for (int i = 0; i < count; i++) {
                Zombie baby = (Zombie) zombie.getType().create(serverLevel, EntitySpawnReason.TRIGGERED);
                if (baby == null) continue;

                double ox = (zombie.getRandom().nextDouble() - 0.5) * 5.0;
                double oz = (zombie.getRandom().nextDouble() - 0.5) * 5.0;
                baby.setPos(zombie.getX() + ox, zombie.getY(), zombie.getZ() + oz);
                baby.setBaby(true);
                baby.addTag(PINATA_TAG);
                baby.finalizeSpawn(serverLevel, difficulty, EntitySpawnReason.TRIGGERED, null);
                serverLevel.addFreshEntity(baby);
            }

            serverLevel.playSound(null, zombie.getX(), zombie.getY(), zombie.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.HOSTILE, 1.0f, 1.0f);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                zombie.getX(), zombie.getY(), zombie.getZ(), 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                zombie.getX(), zombie.getY() + 1, zombie.getZ(),
                30, 1.5, 1.5, 1.5, 0.5);
        });

        OverpoweredMobsLogger.info("Config loaded: zombiePiñataChance=" + config.getZombiePiñataChance() + " zombiePiñataCount=" + config.getZombiePiñataCount());
        LOGGER.info("Overpowered Mobs initialized!");
        OverpoweredMobsLogger.info("Overpowered Mobs initialized");
    }
}
