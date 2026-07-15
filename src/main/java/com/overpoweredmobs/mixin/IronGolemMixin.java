package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IronGolem.class)
public class IronGolemMixin {

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        IronGolem golem = (IronGolem) (Object) this;
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableAngryGolems()) return;
        if (!(golem.level() instanceof ServerLevel serverLevel)) return;

        golem.setPersistentAngerEndTime(Long.MAX_VALUE);

        Player nearest = serverLevel.getNearestPlayer(golem, 64.0);
        if (nearest != null) {
            golem.setPersistentAngerTarget(EntityReference.of(nearest));
        }
    }
}
