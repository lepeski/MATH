package dev.crystalmath.crystalmath.claimer;

import dev.crystalmath.crystalmath.CrystalMathPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class ClaimManager {
    private final CrystalMathPlugin plugin;
    private final File file;
    private FileConfiguration configuration;
    private final Map<UUID, Claim> claims = new LinkedHashMap<>();
    private int defaultRadius;
    private boolean denyOverlap;

    public ClaimManager(CrystalMathPlugin plugin, File file) {
        this.plugin = plugin;
        this.file = file;
    }

    public void load() {
        this.defaultRadius = plugin.getConfig().getInt("claimer.default-radius", 32);
        this.denyOverlap = plugin.getConfig().getBoolean("claimer.deny-overlap", true);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    plugin.getLogger().warning("Unable to create claims file");
                }
            } catch (IOException e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to create claims file", e);
            }
        }
        this.configuration = YamlConfiguration.loadConfiguration(file);
        this.claims.clear();
        ConfigurationSection section = configuration.getConfigurationSection("claims");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Claim claim = Claim.deserialize(section.getConfigurationSection(key));
                if (claim != null) {
                    claims.put(claim.getId(), claim);
                }
            }
        }
    }

    public void save() {
        if (configuration == null) {
            configuration = new YamlConfiguration();
        }
        configuration.set("claims", null);
        ConfigurationSection section = configuration.createSection("claims");
        for (Map.Entry<UUID, Claim> entry : claims.entrySet()) {
            ConfigurationSection claimSection = section.createSection(entry.getKey().toString());
            entry.getValue().serialize(claimSection);
        }
        try {
            configuration.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Could not save claims file", e);
        }
    }

    public Claim createClaim(UUID owner, String ownerName, Location location) {
        if (location.getWorld() == null) {
            return null;
        }
        int radius = defaultRadius;
        Claim claim = new Claim(UUID.randomUUID(), owner, ownerName, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), radius, Instant.now());
        claims.put(claim.getId(), claim);
        save();
        return claim;
    }

    public void removeClaim(UUID claimId) {
        claims.remove(claimId);
        save();
    }

    public Claim getClaim(UUID id) {
        return claims.get(id);
    }

    public Collection<Claim> getClaims() {
        return new ArrayList<>(claims.values());
    }

    public Claim getClaimAt(Location location) {
        if (location == null) {
            return null;
        }
        for (Claim claim : claims.values()) {
            if (claim.contains(location)) {
                return claim;
            }
        }
        return null;
    }

    public boolean isOverlap(Location location) {
        if (!denyOverlap) {
            return false;
        }
        Claim existing = getClaimAt(location);
        return existing != null;
    }

    public int getDefaultRadius() {
        return defaultRadius;
    }

    public boolean shouldShowMessages() {
        return plugin.getConfig().getBoolean("claimer.show-claim-messages", true);
    }
}
