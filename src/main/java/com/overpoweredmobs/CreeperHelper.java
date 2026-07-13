package com.overpoweredmobs;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Creeper;

import java.lang.reflect.Field;

public final class CreeperHelper {
    private static final EntityDataAccessor<Boolean> DATA_IS_POWERED;

    static {
        try {
            Field field = Creeper.class.getDeclaredField("DATA_IS_POWERED");
            field.setAccessible(true);
            DATA_IS_POWERED = (EntityDataAccessor<Boolean>) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access Creeper.DATA_IS_POWERED", e);
        }
    }

    public static void setPowered(Creeper creeper) {
        creeper.getEntityData().set(DATA_IS_POWERED, true);
    }

    private CreeperHelper() {}
}
