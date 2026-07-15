package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.world.entity.monster.EnderMan;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public class EndermanMixin {

    @Inject(method = "isSensitiveToWater", at = @At("HEAD"), cancellable = true)
    private void onIsSensitiveToWater(CallbackInfoReturnable<Boolean> cir) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (config.isEnableWaterEndermen()) {
            cir.setReturnValue(false);
        }
    }
}
