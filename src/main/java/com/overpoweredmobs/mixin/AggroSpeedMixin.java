package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class AggroSpeedMixin {

    @Unique
    private static final int AGGRO_CHECK_INTERVAL = 5;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!mob.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)) return;
        if (!((Object) this instanceof Mob)) return;
        if (mob.tickCount % AGGRO_CHECK_INTERVAL != 0) return;
        if (!(mob.level() instanceof ServerLevel serverLevel)) return;

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableAggro()) return;

        Player nearest = findNearestPlayer(serverLevel, mob);
        if (nearest == null) return;

        double dist = mob.distanceTo(nearest);
        if (dist > config.getAggroSlowRange()) {
            int amplifier = (int) Math.round((config.getAggroSpeedMultiplier() - 1.0) / 0.2);
            if (amplifier < 0) amplifier = 0;
            mob.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, amplifier, false, false, false));
        } else {
            mob.removeEffect(MobEffects.SPEED);
        }
    }

    @Unique
    private static Player findNearestPlayer(ServerLevel level, Mob mob) {
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Player p : level.players()) {
            double d = mob.distanceToSqr(p);
            if (d < nearestDist) {
                nearestDist = d;
                nearest = p;
            }
        }
        return nearest;
    }
}
