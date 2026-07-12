package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobAttributesMixin {
    private static final String BOOSTED_TAG = "opm_boosted";

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData, CallbackInfoReturnable<SpawnGroupData> cir) {
        applyBoosts();
    }

    @Unique
    private void applyBoosts() {
        Mob mob = (Mob) (Object) this;
        if (mob.entityTags().contains(BOOSTED_TAG)) return;

        EntityType<?> type = mob.getType();
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        OverpoweredConfig.MobConfig cfg = config.getFor(type);

        multiplyAttribute(mob, Attributes.MAX_HEALTH, cfg.healthMultiplier());
        multiplyAttribute(mob, Attributes.ATTACK_DAMAGE, cfg.damageMultiplier());
        multiplyAttribute(mob, Attributes.MOVEMENT_SPEED, cfg.speedMultiplier());
        multiplyAttribute(mob, Attributes.ARMOR, cfg.armorMultiplier());
        multiplyAttribute(mob, Attributes.FOLLOW_RANGE, cfg.followRangeMultiplier());

        mob.setHealth(mob.getMaxHealth());
        mob.addTag(BOOSTED_TAG);
    }

    @Unique
    private void multiplyAttribute(Mob mob, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double multiplier) {
        if (multiplier == 1.0) return;
        var instance = mob.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() * multiplier);
        }
    }
}
