package com.overpoweredmobs.mixin;

import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LargeFireball.class)
public interface LargeFireballAccessor {

    @Accessor("explosionPower")
    int getExplosionPower();

    @Accessor("explosionPower")
    void setExplosionPower(int power);
}
