package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IronGolem.class)
public class IronGolemMixin {

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> cir) {
        IronGolem golem = (IronGolem) (Object) this;
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableAngryGolems()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        golem.setPersistentAngerEndTime(Long.MAX_VALUE);

        Player nearest = serverLevel.getNearestPlayer(golem, 64.0);
        if (nearest != null) {
            golem.setPersistentAngerTarget(EntityReference.of(nearest));
        }
    }
}
