package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IronGolem.class)
public class IronGolemMixin {

    @Shadow
    private GoalSelector goalSelector;

    @Shadow
    private GoalSelector targetSelector;

    @Inject(method = "registerGoals", at = @At("HEAD"))
    private void onRegisterGoals(CallbackInfo ci) {
        IronGolem golem = (IronGolem) (Object) this;
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        if (!config.isEnableAngryGolems()) return;

        goalSelector.addGoal(2, new MeleeAttackGoal(golem, 1.0, true));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(golem, Player.class, true));
    }
}
