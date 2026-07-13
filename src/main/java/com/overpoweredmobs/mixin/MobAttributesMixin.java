package com.overpoweredmobs.mixin;

import com.overpoweredmobs.EquipmentHelper;
import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.OverpoweredMobsLogger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Creeper;
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

        if (mob instanceof Creeper creeper && creeper.getRandom().nextDouble() < 0.01) {
            CreeperChargedMixin.setPowered(creeper);
        }

        if (level instanceof ServerLevel serverLevel) {
            equipGear(mob, serverLevel);
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
}
