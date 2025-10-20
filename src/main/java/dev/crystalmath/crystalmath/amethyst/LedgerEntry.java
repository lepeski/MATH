package dev.crystalmath.crystalmath.amethyst;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.UUID;

public final class LedgerEntry {
    private final UUID id;
    private final UUID owner;
    private final String ownerName;
    private final String mintedBy;
    private final Instant mintedAt;
    private final boolean spawned;
    private final ItemStack crystal;

    public LedgerEntry(UUID id, UUID owner, String ownerName, String mintedBy, Instant mintedAt, boolean spawned, ItemStack crystal) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.mintedBy = mintedBy;
        this.mintedAt = mintedAt;
        this.spawned = spawned;
        this.crystal = crystal;
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

    public String getMintedBy() {
        return mintedBy;
    }

    public Instant getMintedAt() {
        return mintedAt;
    }

    public boolean isSpawned() {
        return spawned;
    }

    public ItemStack getCrystal() {
        return crystal == null ? null : crystal.clone();
    }

    public void serialize(ConfigurationSection section) {
        section.set("id", id.toString());
        section.set("owner", owner != null ? owner.toString() : null);
        section.set("ownerName", ownerName);
        section.set("mintedBy", mintedBy);
        section.set("mintedAt", mintedAt.toEpochMilli());
        section.set("spawned", spawned);
        section.set("item", crystal);
    }

    public static LedgerEntry deserialize(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        UUID id = UUID.fromString(section.getString("id"));
        String ownerString = section.getString("owner");
        UUID owner = ownerString != null && !ownerString.isEmpty() ? UUID.fromString(ownerString) : null;
        String ownerName = section.getString("ownerName", "unknown");
        String mintedBy = section.getString("mintedBy", "server");
        long mintedAtLong = section.getLong("mintedAt", System.currentTimeMillis());
        boolean spawned = section.getBoolean("spawned", true);
        ItemStack crystal = section.getItemStack("item");
        return new LedgerEntry(id, owner, ownerName, mintedBy, Instant.ofEpochMilli(mintedAtLong), spawned, crystal);
    }
}
