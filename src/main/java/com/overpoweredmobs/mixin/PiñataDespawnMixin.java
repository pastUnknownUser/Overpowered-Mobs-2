package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class PiñataDespawnMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (mob.entityTags().contains(OverpoweredMobs.PINATA_TAG) && mob.tickCount > 600) {
            mob.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}
