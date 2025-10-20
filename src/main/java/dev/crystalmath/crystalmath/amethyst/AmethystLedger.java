package dev.crystalmath.crystalmath.amethyst;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.crystalmath.crystalmath.claimer.GeodeClaim;

public final class AmethystLedger {
    private final File file;
    private FileConfiguration configuration;
    private final Map<UUID, LedgerEntry> entries = new LinkedHashMap<>();
    private final Map<UUID, GeodeClaim> geodeClaims = new LinkedHashMap<>();

    public AmethystLedger(File file) {
        this.file = file;
    }

    public void load(Plugin plugin) {
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    plugin.getLogger().warning("Unable to create ledger file");
                }
            } catch (IOException e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to create ledger file", e);
            }
        }
        this.configuration = YamlConfiguration.loadConfiguration(file);
        this.entries.clear();
        this.geodeClaims.clear();
        ConfigurationSection section = configuration.getConfigurationSection("entries");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                LedgerEntry entry = LedgerEntry.deserialize(section.getConfigurationSection(key));
                if (entry != null) {
                    this.entries.put(entry.getId(), entry);
                }
            }
        }
        ConfigurationSection geodeSection = configuration.getConfigurationSection("geodes");
        if (geodeSection != null) {
            for (String key : geodeSection.getKeys(false)) {
                GeodeClaim claim = GeodeClaim.deserialize(geodeSection.getConfigurationSection(key));
                if (claim != null) {
                    this.geodeClaims.put(claim.getId(), claim);
                }
            }
        }
    }

    public void save() {
        if (configuration == null) {
            configuration = new YamlConfiguration();
        }
        configuration.set("entries", null);
        ConfigurationSection section = configuration.createSection("entries");
        for (Map.Entry<UUID, LedgerEntry> entry : entries.entrySet()) {
            ConfigurationSection entrySection = section.createSection(entry.getKey().toString());
            entry.getValue().serialize(entrySection);
        }
        configuration.set("geodes", null);
        ConfigurationSection geodeSection = configuration.createSection("geodes");
        for (Map.Entry<UUID, GeodeClaim> entry : geodeClaims.entrySet()) {
            ConfigurationSection claimSection = geodeSection.createSection(entry.getKey().toString());
            entry.getValue().serialize(claimSection);
        }
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addEntry(LedgerEntry entry) {
        entries.put(entry.getId(), entry);
        save();
    }

    public LedgerEntry get(UUID id) {
        return entries.get(id);
    }

    public int size() {
        return entries.size();
    }

    public Collection<LedgerEntry> getEntries() {
        return new ArrayList<>(entries.values());
    }

    public List<LedgerEntry> removeInvalidEntries() {
        List<LedgerEntry> removed = new ArrayList<>();
        List<UUID> invalidIds = new ArrayList<>();
        for (LedgerEntry entry : entries.values()) {
            boolean invalid = entry == null || !entry.isSpawned() || entry.getCrystal() == null;
            if (!invalid) {
                continue;
            }
            invalidIds.add(entry.getId());
        }
        for (UUID id : invalidIds) {
            LedgerEntry removedEntry = entries.remove(id);
            if (removedEntry != null) {
                removed.add(removedEntry);
            }
        }
        if (!removed.isEmpty()) {
            save();
        }
        return removed;
    }

    public void addGeodeClaim(GeodeClaim claim) {
        geodeClaims.put(claim.getId(), claim);
        save();
    }

    public void removeGeodeClaim(UUID id) {
        if (geodeClaims.remove(id) != null) {
            save();
        }
    }

    public Collection<GeodeClaim> getGeodeClaims() {
        return new ArrayList<>(geodeClaims.values());
    }

    public GeodeClaim getGeodeClaim(UUID id) {
        return geodeClaims.get(id);
    }

    public GeodeClaim getGeodeClaim(String world, int chunkX, int chunkZ) {
        for (GeodeClaim claim : geodeClaims.values()) {
            if (claim.getWorldName().equalsIgnoreCase(world)
                && claim.getChunkX() == chunkX
                && claim.getChunkZ() == chunkZ) {
                return claim;
            }
        }
        return null;
    }
}
