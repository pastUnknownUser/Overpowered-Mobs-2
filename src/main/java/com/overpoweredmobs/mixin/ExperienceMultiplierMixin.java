package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class ExperienceMultiplierMixin {
    @ModifyVariable(method = "getBaseExperienceReward", at = @At("RETURN"), ordinal = 0)
    private int multiplyExperience(int xp, ServerLevel level) {
        if (!(((Object) this) instanceof Mob mob)) return xp;
        if (xp <= 0) return xp;

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        OverpoweredConfig.MobConfig cfg = config.getFor(mob.getType());
        return (int) (xp * cfg.xpMultiplier());
    }
}
