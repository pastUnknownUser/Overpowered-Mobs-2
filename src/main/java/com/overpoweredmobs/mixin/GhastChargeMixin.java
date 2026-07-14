package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.world.entity.monster.Ghast;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.Ghast$GhastShootFireballGoal")
public class GhastChargeMixin {

    @Shadow
    private int chargeTime;

    @Shadow
    private Ghast ghast;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (!ghast.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)) return;

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        double speedMult = config.getRangedAttackSpeedMultiplier();
        if (speedMult <= 1.0) return;

        if (chargeTime > 0 && chargeTime < 20) {
            chargeTime = Math.min(20, chargeTime + (int) (speedMult - 1));
        }
    }
}
