package com.overpoweredmobs.mixin;

import com.overpoweredmobs.FenceZoneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class FencePlacementMixin {

    @Inject(method = "setPlacedBy", at = @At("RETURN"))
    private void onSetPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        if (state.getBlock() instanceof FenceBlock || state.getBlock() instanceof FenceGateBlock || state.getBlock() instanceof WallBlock) {
            FenceZoneManager.onFencePlaced(level, pos);
        }
    }
}
