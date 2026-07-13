package com.overpoweredmobs.mixin;

import com.overpoweredmobs.FenceZoneManager;
import com.overpoweredmobs.OverpoweredMobs;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class FenceGuardMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!mob.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)) return;
        if (mob.tickCount % 10 != 0) return;

        FenceZoneManager.evictFromZones(mob);
    }
}
