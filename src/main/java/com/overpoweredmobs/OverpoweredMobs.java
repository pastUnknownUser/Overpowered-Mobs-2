package com.overpoweredmobs;

import com.overpoweredmobs.command.OPMCommand;
import com.overpoweredmobs.config.OverpoweredConfig;
import com.overpoweredmobs.item.OPItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.minecraft.core.registries.BuiltInRegistries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverpoweredMobs implements ModInitializer {
    public static final String MOD_ID = "overpoweredmobs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static OverpoweredConfig config;

    public static OverpoweredConfig getConfig() {
        return config;
    }

    public static void loadConfig() {
        config = OverpoweredConfig.load();
    }

    @Override
    public void onInitialize() {
        loadConfig();
        // OPItems.register();
        // RegistryAttributeHolder.get(BuiltInRegistries.ITEM.key()).addAttribute(RegistryAttribute.OPTIONAL);

        CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, commandSelection) ->
            OPMCommand.register(dispatcher)
        );

        LOGGER.info("Overpowered Mobs initialized!");
    }
}
