package com.overpoweredmobs;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import com.overpoweredmobs.config.OverpoweredConfig;

import java.util.Map;
import java.util.Set;

public final class EquipmentHelper {
    private static final Set<EntityType<?>> NO_EQUIP_TYPES = buildNoEquipTypes();

    private static Set<EntityType<?>> buildNoEquipTypes() {
        return Set.of(
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
    }

    public static boolean isEquippable(EntityType<?> type) {
        return !NO_EQUIP_TYPES.contains(type);
    }

    public static void equipOPGear(Mob mob, RegistryAccess registryAccess) {
        if (!isEquippable(mob.getType())) return;

        HolderGetter<Enchantment> enchants = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);

        boolean isPinata = mob.entityTags().contains(OverpoweredMobs.PINATA_TAG);
        if (!isPinata) {
            setSlot(mob, EquipmentSlot.HEAD, enchanted(enchants, Items.NETHERITE_HELMET, Enchantments.PROTECTION, 10));
            setSlot(mob, EquipmentSlot.CHEST, enchanted(enchants, Items.NETHERITE_CHESTPLATE, Enchantments.PROTECTION, 10));
            setSlot(mob, EquipmentSlot.LEGS, enchanted(enchants, Items.NETHERITE_LEGGINGS, Enchantments.PROTECTION, 10));
            setSlot(mob, EquipmentSlot.FEET, enchanted(enchants, Items.NETHERITE_BOOTS, Enchantments.PROTECTION, 10));
        }

        OverpoweredConfig.MobConfig cfg = OverpoweredMobs.getConfig().getFor(mob.getType());
        String weaponId = cfg.weapon();
        if (weaponId != null) {
            Item weaponItem = BuiltInRegistries.ITEM.getValue(Identifier.tryParse(weaponId));
            if (weaponItem != null) {
                Map<String, Integer> enchantsMap = cfg.weaponEnchantments();
                if (enchantsMap != null && !enchantsMap.isEmpty()) {
                    setSlot(mob, EquipmentSlot.MAINHAND, enchanted(enchantsMap, weaponItem, registryAccess));
                } else {
                    setSlot(mob, EquipmentSlot.MAINHAND, new ItemStack(weaponItem));
                }
                OverpoweredMobsLogger.info("  -> equipped custom weapon: " + weaponId);
            }
        } else if (isRangedMob(mob.getType())) {
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
        mob.setDropChance(slot, 0.0f);
    }

    @SuppressWarnings("unchecked")
    private static ItemStack enchanted(HolderGetter<Enchantment> enchants, Item item, Object... data) {
        ItemStack stack = new ItemStack(item);
        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (int i = 0; i < data.length; i += 2) {
            ResourceKey<Enchantment> key = (ResourceKey<Enchantment>) data[i];
            int level = (int) data[i + 1];
            mutable.set(enchants.getOrThrow(key), level);
        }
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        return stack;
    }

    private static ItemStack enchanted(Map<String, Integer> enchantments, Item item, RegistryAccess registryAccess) {
        ItemStack stack = new ItemStack(item);
        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        HolderGetter<Enchantment> enchants = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id != null) {
                ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
                enchants.get(key).ifPresent(holder -> mutable.set(holder, entry.getValue()));
            }
        }
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        return stack;
    }

    private EquipmentHelper() {}
}
