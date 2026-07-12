package com.overpoweredmobs.mixin;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public class ArrowExplosionMixin {
    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void onHitEntity(EntityHitResult result, CallbackInfo ci) {
        explodeIfOP();
    }

    @Inject(method = "onHitBlock", at = @At("HEAD"))
    private void onHitBlock(BlockHitResult result, CallbackInfo ci) {
        explodeIfOP();
    }

    private void explodeIfOP() {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        if (arrow.entityTags().contains("opexplosive")) {
            Level level = arrow.level();
            if (!level.isClientSide()) {
                level.explode(arrow, arrow.getX(), arrow.getY(), arrow.getZ(), 4.0f, Level.ExplosionInteraction.MOB);
            }
        }
    }
}
