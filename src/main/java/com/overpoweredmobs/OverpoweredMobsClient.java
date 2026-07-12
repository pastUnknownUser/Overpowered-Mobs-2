package com.overpoweredmobs;

import net.fabricmc.api.ClientModInitializer;

public class OverpoweredMobsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        OverpoweredMobs.LOGGER.info("Overpowered Mobs client initialized!");
    }
}
