package com.overpoweredmobs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class OverpoweredMobsLogger {
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static Path logFile;

    private OverpoweredMobsLogger() {}

    public static void init(Path gameDir) {
        logFile = gameDir.resolve("logs").resolve("overpoweredmobs.log");
        try {
            Files.createDirectories(logFile.getParent());
            Files.writeString(logFile, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            info("Logger initialized");
        } catch (IOException e) {
            logFile = null;
        }
    }

    public static void info(String msg) {
        write("INFO", msg);
    }

    public static void warn(String msg) {
        write("WARN", msg);
    }

    public static void error(String msg) {
        write("ERROR", msg);
    }

    private static void write(String level, String msg) {
        if (logFile == null) return;
        try {
            String line = String.format("[%s] [%s] %s%n", LocalDateTime.now().format(TIMESTAMP), level, msg);
            Files.writeString(logFile, line, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }
}
