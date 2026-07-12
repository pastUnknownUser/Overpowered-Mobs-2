package com.overpoweredmobs;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Set;

public final class EquipmentHelper {
    private static final Set<EntityType<?>> NO_EQUIP_TYPES = Set.of(
        EntityType.CREEPER,
        EntityType.SPIDER,
        EntityType.CAVE_SPIDER,
        EntityType.SLIME,
        EntityType.MAGMA_CUBE,
        EntityType.ENDERMAN,
        EntityType.SILVERFISH,
        EntityType.ENDERMITE,
        EntityType.BLAZE,
        EntityType.GHAST,
        EntityType.GUARDIAN,
        EntityType.ELDER_GUARDIAN,
        EntityType.WITCH,
        EntityType.PHANTOM
    );

    public static void equipOPGear(Mob mob, RegistryAccess registryAccess) {
        if (NO_EQUIP_TYPES.contains(mob.getType())) return;

        HolderGetter<Enchantment> enchants = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);

        setSlot(mob, EquipmentSlot.HEAD, enchanted(enchants, Items.NETHERITE_HELMET, Enchantments.PROTECTION, 10));
        setSlot(mob, EquipmentSlot.CHEST, enchanted(enchants, Items.NETHERITE_CHESTPLATE, Enchantments.PROTECTION, 10));
        setSlot(mob, EquipmentSlot.LEGS, enchanted(enchants, Items.NETHERITE_LEGGINGS, Enchantments.PROTECTION, 10));
        setSlot(mob, EquipmentSlot.FEET, enchanted(enchants, Items.NETHERITE_BOOTS, Enchantments.PROTECTION, 10));

        if (isRangedMob(mob.getType())) {
            setSlot(mob, EquipmentSlot.MAINHAND, enchanted(enchants, Items.BOW, Enchantments.POWER, 10, Enchantments.PUNCH, 3, Enchantments.FLAME, 1));
            OverpoweredMobsLogger.info("  -> equipped OP bow");
        } else {
            setSlot(mob, EquipmentSlot.MAINHAND, enchanted(enchants, Items.NETHERITE_SWORD, Enchantments.SHARPNESS, 10, Enchantments.FIRE_ASPECT, 3));
            OverpoweredMobsLogger.info("  -> equipped OP sword");
        }
    }

    private static boolean isRangedMob(EntityType<?> type) {
        return type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.BOGGED;
    }

    private static void setSlot(Mob mob, EquipmentSlot slot, ItemStack stack) {
        mob.setItemSlot(slot, stack);
        mob.setGuaranteedDrop(slot);
    }

    private static ItemStack enchanted(HolderGetter<Enchantment> enchants, Item item, Object... data) {
        ItemStack stack = new ItemStack(item);
        for (int i = 0; i < data.length; i += 2) {
            ResourceKey<Enchantment> key = (ResourceKey<Enchantment>) data[i];
            int level = (int) data[i + 1];
            stack.enchant(enchants.getOrThrow(key), level);
        }
        return stack;
    }

    private EquipmentHelper() {}
}
