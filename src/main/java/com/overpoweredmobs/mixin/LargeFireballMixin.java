package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LargeFireball.class)
public class LargeFireballMixin {

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;DDDLnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        LargeFireball fireball = (LargeFireball) (Object) this;
        Entity owner = fireball.getOwner();
        if (!(owner instanceof Mob mob)) return;
        if (!mob.entityTags().contains(OverpoweredMobs.BOOSTED_TAG)) return;

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        double mult = config.getGhastExplosionMultiplier();
        if (mult == 1.0) return;

        int power = ((LargeFireballAccessor) this).getExplosionPower();
        ((LargeFireballAccessor) this).setExplosionPower((int) Math.ceil(power * mult));
    }
}
