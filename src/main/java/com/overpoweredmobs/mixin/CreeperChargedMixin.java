package com.overpoweredmobs.mixin;

import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Creeper.class)
public class CreeperChargedMixin {

    @ModifyArg(
        method = "explodeCreeper",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)V"),
        index = 4
    )
    private float modifyExplosionPower(float originalPower) {
        return ((Creeper)(Object)this).isPowered() ? 9.0f : 6.0f;
    }
}
