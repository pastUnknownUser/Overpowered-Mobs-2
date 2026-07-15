package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.phys.EntityHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBullet.class)
public class ShulkerBulletMixin {

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void onHitEntity(EntityHitResult result, CallbackInfo ci) {
        if (!(result.getEntity() instanceof LivingEntity living)) return;

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        double mult = config.getShulkerLevitationDurationMultiplier();
        if (mult == 1.0) return;

        MobEffectInstance lev = living.getEffect(MobEffects.LEVITATION);
        if (lev != null) {
            int newDuration = (int) Math.ceil(lev.getDuration() * mult);
            living.addEffect(new MobEffectInstance(
                MobEffects.LEVITATION, newDuration, lev.getAmplifier(),
                lev.isAmbient(), lev.isVisible(), lev.showIcon()
            ));
        }
    }
}
