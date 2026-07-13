package com.overpoweredmobs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.overpoweredmobs.OverpoweredMobs;
import com.overpoweredmobs.OverpoweredMobsLogger;
import com.overpoweredmobs.config.OverpoweredConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public class OPMCommand {
    private static final String[] ATTRS = {"health", "damage", "speed", "armor", "followRange", "xp", "drops"};

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("opm")
            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
            .then(Commands.literal("set")
                .then(Commands.argument("mob", StringArgumentType.word())
                    .then(Commands.argument("attribute", StringArgumentType.word())
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.1, 100.0))
                            .executes(OPMCommand::executeSet)))))
            .then(Commands.literal("reload")
                .executes(OPMCommand::executeReload))
            .then(Commands.literal("status")
                .executes(OPMCommand::executeStatus))
            .then(Commands.literal("reset")
                .executes(OPMCommand::executeReset))
            .then(Commands.literal("test")
                .executes(OPMCommand::executeTest))
        );
    }

    private static int executeSet(CommandContext<CommandSourceStack> ctx) {
        String mobStr = StringArgumentType.getString(ctx, "mob");
        String attr = StringArgumentType.getString(ctx, "attribute");
        double value = DoubleArgumentType.getDouble(ctx, "value");

        EntityType<?> type = findEntityType(mobStr);
        if (type == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown entity type: " + mobStr));
            return 0;
        }

        OverpoweredConfig config = OverpoweredMobs.getConfig();
        OverpoweredConfig.MobConfig cfg = config.getFor(type);
        cfg.set(attr, value);
        config.setFor(type, cfg);
        config.save();

        ctx.getSource().sendSuccess(() ->
            Component.literal("Set " + mobStr + " " + attr + " to " + value), true);
        return 1;
    }

    private static int executeReload(CommandContext<CommandSourceStack> ctx) {
        OverpoweredMobs.loadConfig();
        ctx.getSource().sendSuccess(() ->
            Component.literal("Config reloaded"), true);
        return 1;
    }

    private static int executeStatus(CommandContext<CommandSourceStack> ctx) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        ctx.getSource().sendSuccess(() ->
            Component.literal("=== Default multipliers ==="), false);

        OverpoweredConfig.MobConfig defaults = config.getDefaults();
        for (String attr : ATTRS) {
            double val = defaults.get(attr);
            ctx.getSource().sendSuccess(() ->
                Component.literal("  " + attr + ": " + val), false);
        }

        ctx.getSource().sendSuccess(() ->
            Component.literal("=== Per-mob overrides ==="), false);

        for (Map.Entry<String, OverpoweredConfig.MobConfig> entry : config.getMobs().entrySet()) {
            String key = entry.getKey();
            OverpoweredConfig.MobConfig mc = entry.getValue();
            ctx.getSource().sendSuccess(() ->
                Component.literal("  " + key + ":"), false);
            for (String attr : ATTRS) {
                double val = mc.get(attr);
                ctx.getSource().sendSuccess(() ->
                    Component.literal("    " + attr + ": " + val), false);
            }
        }
        return 1;
    }

    private static int executeReset(CommandContext<CommandSourceStack> ctx) {
        OverpoweredConfig.reset();
        ctx.getSource().sendSuccess(() ->
            Component.literal("Config reset to defaults"), true);
        return 1;
    }

    private static int executeTest(CommandContext<CommandSourceStack> ctx) {
        OverpoweredConfig config = OverpoweredMobs.getConfig();
        boolean now = !config.isTestMode();
        config.setTestMode(now);
        config.save();
        ctx.getSource().sendSuccess(() ->
            Component.literal("Test mode " + (now ? "enabled" : "disabled") + " — all random chances forced to 100%"), true);
        OverpoweredMobsLogger.info("Test mode " + (now ? "enabled" : "disabled"));
        return 1;
    }

    private static EntityType<?> findEntityType(String str) {
        if (!str.contains(":")) str = "minecraft:" + str;
        Identifier id = Identifier.tryParse(str);
        if (id == null) return null;
        return BuiltInRegistries.ENTITY_TYPE.getValue(id);
    }
}
