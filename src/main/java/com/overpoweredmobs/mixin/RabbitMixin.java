package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.level.ServerLevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Rabbit.class)
public class RabbitMixin {

    @Shadow
    private static EntityDataAccessor<Integer> DATA_TYPE_ID;

    @Unique
    private static final int EVIL_VARIANT_ID = 99;

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> cir) {
        Rabbit rabbit = (Rabbit) (Object) this;
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (config.isEnableEvilBunnies()) {
            rabbit.getEntityData().set(DATA_TYPE_ID, EVIL_VARIANT_ID);
        }
    }
}
