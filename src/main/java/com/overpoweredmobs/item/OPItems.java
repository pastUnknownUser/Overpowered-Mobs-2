package com.overpoweredmobs.item;

import com.overpoweredmobs.OverpoweredMobs;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;

public class OPItems {
    public static final Identifier OP_SWORD_ID = Identifier.fromNamespaceAndPath(OverpoweredMobs.MOD_ID, "op_sword");
    public static final Identifier OP_BOW_ID = Identifier.fromNamespaceAndPath(OverpoweredMobs.MOD_ID, "op_bow");
    public static final Identifier OP_PICKAXE_ID = Identifier.fromNamespaceAndPath(OverpoweredMobs.MOD_ID, "op_pickaxe");
    public static final Identifier OP_AXE_ID = Identifier.fromNamespaceAndPath(OverpoweredMobs.MOD_ID, "op_axe");
    public static final Identifier OP_SHOVEL_ID = Identifier.fromNamespaceAndPath(OverpoweredMobs.MOD_ID, "op_shovel");
    public static final Identifier OP_HOE_ID = Identifier.fromNamespaceAndPath(OverpoweredMobs.MOD_ID, "op_hoe");
    public static final Identifier OP_HELMET_ID = Identifier.fromNamespaceAndPath(OverpoweredMobs.MOD_ID, "op_helmet");
    public static final Identifier OP_CHESTPLATE_ID = Identifier.fromNamespaceAndPath(OverpoweredMobs.MOD_ID, "op_chestplate");
    public static final Identifier OP_LEGGINGS_ID = Identifier.fromNamespaceAndPath(OverpoweredMobs.MOD_ID, "op_leggings");
    public static final Identifier OP_BOOTS_ID = Identifier.fromNamespaceAndPath(OverpoweredMobs.MOD_ID, "op_boots");
    public static final Identifier OP_TAB_ID = Identifier.fromNamespaceAndPath(OverpoweredMobs.MOD_ID, "op_tab");

    private static final ToolMaterial OP_TOOL = new ToolMaterial(
        net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
        99999,
        64.0f,
        20.0f,
        30,
        net.minecraft.tags.ItemTags.NETHERITE_TOOL_MATERIALS
    );

    private static final ArmorMaterial OP_ARMOR = new ArmorMaterial(
        99999,
        makeDefenseMap(50, 50, 50, 50),
        30,
        net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_NETHERITE,
        10.0f,
        5.0f,
        net.minecraft.tags.ItemTags.REPAIRS_NETHERITE_ARMOR,
        EquipmentAssets.NETHERITE
    );

    public static final Item OP_SWORD = new OPSwordItem(new Item.Properties()
        .sword(OP_TOOL, 30.0f, -1.0f)
        .rarity(Rarity.EPIC)
        .fireResistant()
    );

    public static final Item OP_BOW = new OPBowItem(new Item.Properties()
        .durability(99999)
        .rarity(Rarity.EPIC)
        .fireResistant()
    );

    public static final Item OP_PICKAXE = new OPToolItem(
        OP_TOOL,
        20.0f,
        -1.0f,
        net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE,
        new Item.Properties()
            .pickaxe(OP_TOOL, 20.0f, -1.0f)
            .rarity(Rarity.EPIC)
            .fireResistant()
    );

    public static final Item OP_AXE = new OPToolItem(
        OP_TOOL,
        18.0f,
        -1.0f,
        net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE,
        new Item.Properties()
            .axe(OP_TOOL, 18.0f, -1.0f)
            .rarity(Rarity.EPIC)
            .fireResistant()
    );

    public static final Item OP_SHOVEL = new OPToolItem(
        OP_TOOL,
        15.0f,
        -1.0f,
        net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL,
        new Item.Properties()
            .shovel(OP_TOOL, 15.0f, -1.0f)
            .rarity(Rarity.EPIC)
            .fireResistant()
    );

    public static final Item OP_HOE = new OPToolItem(
        OP_TOOL,
        10.0f,
        0.0f,
        net.minecraft.tags.BlockTags.MINEABLE_WITH_HOE,
        new Item.Properties()
            .hoe(OP_TOOL, 10.0f, 0.0f)
            .rarity(Rarity.EPIC)
            .fireResistant()
    );

    public static final Item OP_HELMET = new Item(new Item.Properties()
        .humanoidArmor(OP_ARMOR, ArmorType.HELMET)
        .rarity(Rarity.EPIC)
        .fireResistant()
    );

    public static final Item OP_CHESTPLATE = new Item(new Item.Properties()
        .humanoidArmor(OP_ARMOR, ArmorType.CHESTPLATE)
        .rarity(Rarity.EPIC)
        .fireResistant()
    );

    public static final Item OP_LEGGINGS = new Item(new Item.Properties()
        .humanoidArmor(OP_ARMOR, ArmorType.LEGGINGS)
        .rarity(Rarity.EPIC)
        .fireResistant()
    );

    public static final Item OP_BOOTS = new Item(new Item.Properties()
        .humanoidArmor(OP_ARMOR, ArmorType.BOOTS)
        .rarity(Rarity.EPIC)
        .fireResistant()
    );

    public static final CreativeModeTab OP_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 6)
        .title(Component.translatable("itemGroup.overpoweredmobs.op_tab"))
        .icon(() -> new ItemStack(OP_SWORD))
        .displayItems((params, output) -> {
            output.accept(OP_SWORD);
            output.accept(OP_BOW);
            output.accept(OP_PICKAXE);
            output.accept(OP_AXE);
            output.accept(OP_SHOVEL);
            output.accept(OP_HOE);
            output.accept(OP_HELMET);
            output.accept(OP_CHESTPLATE);
            output.accept(OP_LEGGINGS);
            output.accept(OP_BOOTS);
        })
        .build();

    public static void register() {
        registerItem(OP_SWORD_ID, OP_SWORD);
        registerItem(OP_BOW_ID, OP_BOW);
        registerItem(OP_PICKAXE_ID, OP_PICKAXE);
        registerItem(OP_AXE_ID, OP_AXE);
        registerItem(OP_SHOVEL_ID, OP_SHOVEL);
        registerItem(OP_HOE_ID, OP_HOE);
        registerItem(OP_HELMET_ID, OP_HELMET);
        registerItem(OP_CHESTPLATE_ID, OP_CHESTPLATE);
        registerItem(OP_LEGGINGS_ID, OP_LEGGINGS);
        registerItem(OP_BOOTS_ID, OP_BOOTS);

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, OP_TAB_ID, OP_TAB);
    }

    private static void registerItem(Identifier id, Item item) {
        Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    private static EnumMap<ArmorType, Integer> makeDefenseMap(int head, int chest, int legs, int feet) {
        EnumMap<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.HELMET, head);
        map.put(ArmorType.CHESTPLATE, chest);
        map.put(ArmorType.LEGGINGS, legs);
        map.put(ArmorType.BOOTS, feet);
        return map;
    }
}
