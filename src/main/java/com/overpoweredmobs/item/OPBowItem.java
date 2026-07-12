package com.overpoweredmobs.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class OPBowItem extends BowItem {
    private static final int ARROW_COUNT = 5;
    private static final float SPREAD = 15.0f;

    public OPBowItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, LivingEntity target) {
        Level level = shooter.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        for (int i = 0; i < ARROW_COUNT; i++) {
            Arrow arrow = new Arrow(level, shooter, new ItemStack(Items.ARROW), new ItemStack(Items.ARROW));
            arrow.setBaseDamage(15.0);
            arrow.setSoundEvent(net.minecraft.sounds.SoundEvents.CROSSBOW_HIT);

            float spread = (i - (ARROW_COUNT - 1) / 2.0f) * SPREAD / ARROW_COUNT;
            float yaw = shooter.getYRot() + spread;
            float pitch = shooter.getXRot() + (i % 2 == 0 ? -2 : 2);
            double x = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
            double y = -Math.sin(Math.toRadians(pitch));
            double z = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
            arrow.shoot(x, y, z, velocity * 3.0f, inaccuracy);

            arrow.addTag("opexplosive");
            serverLevel.addFreshEntity(arrow);
        }
    }

    @Override
    public int getDefaultProjectileRange() {
        return 128;
    }
}
