package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class DropMultiplierMixin {
    @Unique
    private static final int DROP_RADIUS = 5;

    @Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
    private void afterDropAllDeathLoot(ServerLevel level, DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof Mob mob)) return;

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        OverpoweredConfig.MobConfig cfg = config.getFor(mob.getType());
        double multiplier = cfg.dropMultiplier();
        if (multiplier <= 1.0) return;

        double mx = entity.getX(), my = entity.getY(), mz = entity.getZ();
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, entity.getBoundingBox().inflate(DROP_RADIUS))) {
            if (item.isAlive() && item.distanceToSqr(mx, my, mz) < DROP_RADIUS * DROP_RADIUS) {
                ItemStack stack = item.getItem().copy();
                int extraCount = (int) Math.floor(stack.getCount() * (multiplier - 1.0));
                if (extraCount > 0) {
                    stack.setCount(extraCount);
                    ItemEntity extra = new ItemEntity(level, item.getX(), item.getY(), item.getZ(), stack);
                    extra.setDeltaMovement(item.getDeltaMovement());
                    level.addFreshEntity(extra);
                }
            }
        }
    }
}
