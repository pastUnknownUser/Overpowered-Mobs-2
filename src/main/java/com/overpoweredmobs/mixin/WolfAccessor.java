package com.overpoweredmobs.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.wolf.Wolf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Wolf.class)
public interface WolfAccessor {

    @Accessor("DATA_ANGER_END_TIME")
    EntityDataAccessor<Long> getDataAngerEndTime();
}
