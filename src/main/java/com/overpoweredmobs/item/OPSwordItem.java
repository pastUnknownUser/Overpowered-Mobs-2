package com.overpoweredmobs.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OPSwordItem extends Item {
    private static final double AREA_RADIUS = 5.0;
    private static final float AREA_DAMAGE = 10.0f;

    public OPSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = target.level();
        if (!level.isClientSide()) {
            Vec3 pos = target.position();
            AABB box = AABB.ofSize(pos, AREA_RADIUS * 2, AREA_RADIUS * 2, AREA_RADIUS * 2);
            for (Entity entity : level.getEntities(target, box, e -> e != attacker && e instanceof LivingEntity && e.isAlive())) {
                entity.hurt(attacker.damageSources().playerAttack(attacker instanceof net.minecraft.world.entity.player.Player p ? p : null), AREA_DAMAGE);
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }
}
