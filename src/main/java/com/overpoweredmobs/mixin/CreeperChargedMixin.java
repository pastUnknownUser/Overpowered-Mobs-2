package com.overpoweredmobs.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Creeper.class)
public class CreeperChargedMixin {

    @Shadow
    private static EntityDataAccessor<Boolean> DATA_IS_POWERED;

    public static void setPowered(Creeper creeper) {
        creeper.getEntityData().set(DATA_IS_POWERED, true);
    }

    @ModifyArg(
        method = "explodeCreeper",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/explosion/Explosion;"),
        index = 4
    )
    private float modifyExplosionPower(float originalPower) {
        return ((Creeper)(Object)this).isPowered() ? 9.0f : 6.0f;
    }
}
