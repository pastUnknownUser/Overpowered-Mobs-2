package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public class WolfMixin {

    @Shadow
    private static EntityDataAccessor<Long> DATA_ANGER_END_TIME;

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> cir) {
        Wolf wolf = (Wolf) (Object) this;
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableAngryWolves()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        wolf.getEntityData().set(DATA_ANGER_END_TIME, Long.MAX_VALUE);

        Player nearest = serverLevel.getNearestPlayer(wolf, 64.0);
        if (nearest != null) {
            wolf.setPersistentAngerTarget(EntityReference.of(nearest));
        }
    }
}
