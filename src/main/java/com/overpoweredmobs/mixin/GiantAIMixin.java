package com.overpoweredmobs.mixin;

import com.overpoweredmobs.mixin.MobAccessor;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Giant.class)
public class GiantAIMixin {

    @Inject(method = "registerGoals", at = @At("HEAD"))
    private void onRegisterGoals(CallbackInfo ci) {
        Giant giant = (Giant) (Object) this;
        var attack = giant.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(20.0);
        }
        var goals = ((MobAccessor) giant).getGoalSelector();
        goals.addGoal(1, new MeleeAttackGoal(giant, 1.0, true));
        goals.addGoal(2, new LookAtPlayerGoal(giant, Player.class, 16.0f));
        goals.addGoal(3, new WaterAvoidingRandomStrollGoal(giant, 0.8));
        goals.addGoal(4, new RandomLookAroundGoal(giant));
    }
}
