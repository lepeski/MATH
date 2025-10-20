package dev.crystalmath.crystalmath.common;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Logger;

public final class Logging {
    private Logging() {
    }

    public static void logConfigured(FileConfiguration config, Logger logger, String path, String message) {
        if (config.getBoolean("logging." + path, true)) {
            logger.info(message);
        }
    }
}
