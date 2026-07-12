package com.overpoweredmobs.mixin;

import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(Mob.class)
public class MobAttributesMixin {
    private static final String BOOSTED_TAG = "opm_boosted";
    private static final double OP_GEAR_CHANCE = 0.40;

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

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData, CallbackInfoReturnable<SpawnGroupData> cir) {
        Mob mob = (Mob) (Object) this;
        if (mob.getType().getCategory() != MobCategory.MONSTER) return;
        applyBoosts();
        if (reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.SPAWNER || reason == EntitySpawnReason.STRUCTURE) {
            // equipOPGear(level); // disabled for diagnostic
        }
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
    private void equipOPGear(ServerLevelAccessor level) {
        Mob mob = (Mob) (Object) this;
        if (NO_EQUIP_TYPES.contains(mob.getType())) return;

        RandomSource random = mob.getRandom();
        if (random.nextDouble() >= OP_GEAR_CHANCE) return;

        HolderGetter<Enchantment> enchants = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        equipSlot(mob, random, EquipmentSlot.HEAD, enchanted(enchants, Items.NETHERITE_HELMET, Enchantments.PROTECTION, 10));
        equipSlot(mob, random, EquipmentSlot.CHEST, enchanted(enchants, Items.NETHERITE_CHESTPLATE, Enchantments.PROTECTION, 10));
        equipSlot(mob, random, EquipmentSlot.LEGS, enchanted(enchants, Items.NETHERITE_LEGGINGS, Enchantments.PROTECTION, 10));
        equipSlot(mob, random, EquipmentSlot.FEET, enchanted(enchants, Items.NETHERITE_BOOTS, Enchantments.PROTECTION, 10));

        ItemStack held = mob.getItemBySlot(EquipmentSlot.MAINHAND);
        if (held.getItem() instanceof BowItem || held.getItem() instanceof CrossbowItem) {
            equipSlot(mob, random, EquipmentSlot.MAINHAND, enchanted(enchants, Items.BOW, Enchantments.POWER, 10, Enchantments.PUNCH, 3, Enchantments.FLAME, 1));
        } else {
            equipSlot(mob, random, EquipmentSlot.MAINHAND, enchanted(enchants, Items.NETHERITE_SWORD, Enchantments.SHARPNESS, 10, Enchantments.FIRE_ASPECT, 3));
        }
    }

    @Unique
    private ItemStack enchanted(HolderGetter<Enchantment> enchants, Item item, Object... data) {
        ItemStack stack = new ItemStack(item);
        for (int i = 0; i < data.length; i += 2) {
            ResourceKey<Enchantment> key = (ResourceKey<Enchantment>) data[i];
            int level = (int) data[i + 1];
            stack.enchant(enchants.getOrThrow(key), level);
        }
        return stack;
    }

    @Unique
    private void equipSlot(Mob mob, RandomSource random, EquipmentSlot slot, ItemStack stack) {
        if (random.nextDouble() < 0.5) return;
        mob.setItemSlot(slot, stack);
        mob.setGuaranteedDrop(slot);
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
