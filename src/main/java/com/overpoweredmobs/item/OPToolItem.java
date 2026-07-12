package com.overpoweredmobs.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public class OPToolItem extends Item {
    private final float attackDamage;
    private final float attackSpeed;
    private final TagKey<Block> effectiveBlocks;
    private static final int HARVEST_RADIUS = 2;

    public OPToolItem(ToolMaterial material, float attackDamage, float attackSpeed, TagKey<Block> effectiveBlocks, Properties properties) {
        super(properties);
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.effectiveBlocks = effectiveBlocks;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(effectiveBlocks)) {
            return 9999.0f;
        }
        return 1.0f;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player player && player.isCreative()) {
            return super.mineBlock(stack, level, state, pos, entity);
        }
        if (!level.isClientSide() && entity instanceof Player player) {
            Direction direction = player.getDirection();
            harvestArea(level, pos, direction, player);
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }

    private void harvestArea(Level level, BlockPos center, Direction direction, Player player) {
        Set<BlockPos> toHarvest = new HashSet<>();
        Direction.Axis axis = direction.getAxis();

        for (int dx = -HARVEST_RADIUS; dx <= HARVEST_RADIUS; dx++) {
            for (int dy = -HARVEST_RADIUS; dy <= HARVEST_RADIUS; dy++) {
                for (int dz = -HARVEST_RADIUS; dz <= HARVEST_RADIUS; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    BlockPos target = switch (axis) {
                        case X -> center.offset(0, dy, dz);
                        case Y -> center.offset(dx, 0, dz);
                        case Z -> center.offset(dx, dy, 0);
                    };

                    BlockState targetState = level.getBlockState(target);
                    if (targetState.is(effectiveBlocks)) {
                        toHarvest.add(target);
                    }
                }
            }
        }

        for (BlockPos pos : toHarvest) {
            BlockState targetState = level.getBlockState(pos);
            if (!targetState.isAir()) {
                level.destroyBlock(pos, true, player);
                player.getMainHandItem().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }
        }
    }
}
