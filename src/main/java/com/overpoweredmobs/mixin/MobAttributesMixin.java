package com.overpoweredmobs.mixin;

import com.overpoweredmobs.CreeperHelper;
import com.overpoweredmobs.EquipmentHelper;
import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.OverpoweredMobsLogger;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobAttributesMixin {

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData, CallbackInfoReturnable<SpawnGroupData> cir) {
        Mob mob = (Mob) (Object) this;
        if (mob.getType().getCategory() != MobCategory.MONSTER) return;

        OverpoweredMobsLogger.info("finalizeSpawn for " + mob.getType() + " at " + mob.blockPosition() + " reason=" + reason);
        OverpoweredMobs.applyBoosts(mob);

        if (mob instanceof Creeper creeper) {
            CreeperHelper.setPowered(creeper);
        }

        if (level instanceof ServerLevel serverLevel) {
            equipGear(mob, serverLevel);
            trySpawnCavalry(mob, serverLevel, difficulty, reason);
        }
    }

    @Unique
    private static void equipGear(Mob mob, ServerLevel level) {
        level.getServer().execute(() -> {
            if (!mob.isAlive()) return;
            OverpoweredMobsLogger.info("  -> equipping gear (deferred) for " + mob.getType());
            EquipmentHelper.equipOPGear(mob, level.registryAccess());
        });
    }

    @Unique
    private static void trySpawnCavalry(Mob rider, ServerLevel level, DifficultyInstance difficulty, EntitySpawnReason reason) {
        Identifier riderKey = BuiltInRegistries.ENTITY_TYPE.getKey(rider.getType());
        if (riderKey == null) return;

        String riderId = riderKey.toString();
        for (OverpoweredConfig.CavalryEntry entry : OverpoweredMobs.getConfig().getCavalry()) {
            if (!entry.rider().equals(riderId)) continue;
            if (rider.getRandom().nextDouble() >= entry.chance()) continue;

            Identifier mountKey = Identifier.tryParse(entry.mount());
            if (mountKey == null) return;
            EntityType<?> mountType = BuiltInRegistries.ENTITY_TYPE.getValue(mountKey);
            if (mountType == null) return;

            Mob mount = (Mob) mountType.create(level, EntitySpawnReason.JOCKEY);
            if (mount == null) return;

            mount.setPos(rider.getX(), rider.getY(), rider.getZ());
            mount.finalizeSpawn(level, difficulty, EntitySpawnReason.JOCKEY, null);
            mount.addTag("opm_cavalry_mount");
            level.addFreshEntity(mount);

            if (entry.baby() && rider instanceof Zombie zombie) {
                zombie.setBaby(true);
            }

            rider.startRiding(mount);
            OverpoweredMobsLogger.info("  -> cavalry: " + riderId + " riding " + entry.mount());
            return;
        }
    }
}
