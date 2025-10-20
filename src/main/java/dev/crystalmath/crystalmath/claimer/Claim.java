package dev.crystalmath.crystalmath.claimer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public final class Claim {
    private final UUID id;
    private final UUID owner;
    private final String ownerName;
    private final String worldName;
    private final int x;
    private final int y;
    private final int z;
    private final int radius;
    private final Instant createdAt;

    public Claim(UUID id, UUID owner, String ownerName, String worldName, int x, int y, int z, int radius, Instant createdAt) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getRadius() {
        return radius;
    }

    public Location getCenter() {
        World world = Bukkit.getWorld(worldName);
        return world == null ? null : new Location(world, x + 0.5, y, z + 0.5);
    }

    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null || worldName == null) {
            return false;
        }
        if (!location.getWorld().getName().equals(worldName)) {
            return false;
        }
        double dx = Math.abs(location.getBlockX() - x);
        double dz = Math.abs(location.getBlockZ() - z);
        return dx <= radius && dz <= radius;
    }

    public void serialize(ConfigurationSection section) {
        section.set("id", id.toString());
        section.set("owner", owner != null ? owner.toString() : null);
        section.set("ownerName", ownerName);
        section.set("world", worldName);
        section.set("x", x);
        section.set("y", y);
        section.set("z", z);
        section.set("radius", radius);
        section.set("createdAt", createdAt.toEpochMilli());
    }

    public static @Nullable Claim deserialize(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        UUID id = UUID.fromString(section.getString("id"));
        String ownerString = section.getString("owner");
        UUID owner = ownerString != null && !ownerString.isEmpty() ? UUID.fromString(ownerString) : null;
        String ownerName = section.getString("ownerName", "unknown");
        String world = section.getString("world");
        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");
        int radius = section.getInt("radius", 32);
        long createdAt = section.getLong("createdAt", System.currentTimeMillis());
        return new Claim(id, owner, ownerName, world, x, y, z, radius, Instant.ofEpochMilli(createdAt));
    }
}
