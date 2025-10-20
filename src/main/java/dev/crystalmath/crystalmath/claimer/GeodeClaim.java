package dev.crystalmath.crystalmath.claimer;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public final class GeodeClaim {
    private final UUID id;
    private final String worldName;
    private final int chunkX;
    private final int chunkZ;
    private final String createdBy;
    private final Instant createdAt;

    public GeodeClaim(UUID id, String worldName, int chunkX, int chunkZ, String createdBy, Instant createdAt) {
        this.id = id;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void serialize(ConfigurationSection section) {
        section.set("id", id.toString());
        section.set("world", worldName);
        section.set("chunkX", chunkX);
        section.set("chunkZ", chunkZ);
        section.set("createdBy", createdBy);
        section.set("createdAt", createdAt.toEpochMilli());
    }

    public static @Nullable GeodeClaim deserialize(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        UUID id = UUID.fromString(section.getString("id"));
        String world = section.getString("world");
        int chunkX = section.getInt("chunkX");
        int chunkZ = section.getInt("chunkZ");
        String createdBy = section.getString("createdBy", "server");
        long createdAt = section.getLong("createdAt", System.currentTimeMillis());
        return new GeodeClaim(id, world, chunkX, chunkZ, createdBy, Instant.ofEpochMilli(createdAt));
    }
}
