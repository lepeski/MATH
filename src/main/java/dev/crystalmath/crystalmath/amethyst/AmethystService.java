package dev.crystalmath.crystalmath.amethyst;

import dev.crystalmath.crystalmath.CrystalMathPlugin;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class AmethystService {
    private final CrystalMathPlugin plugin;
    private final AmethystLedger ledger;
    private NamespacedKey idKey;
    private NamespacedKey markerKey;
    private int maxMinted;
    private Sound mintSound;
    private boolean dropOnFullInventory;
    private List<String> lore;
    private String displayName;

    public AmethystService(CrystalMathPlugin plugin, AmethystLedger ledger) {
        this.plugin = plugin;
        this.ledger = ledger;
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();
        this.idKey = new NamespacedKey(plugin, "minted-crystal-id");
        this.markerKey = new NamespacedKey(plugin, "minted-crystal");
        this.maxMinted = config.getInt("amethyst.max-minted", 64);
        this.displayName = config.getString("amethyst.item-name", "Minted Amethyst Crystal");
        this.lore = config.getStringList("amethyst.item-lore");
        this.mintSound = parseSound(config.getString("amethyst.mint-sound", Sound.ENTITY_PLAYER_LEVELUP.name()));
        this.dropOnFullInventory = config.getBoolean("amethyst.drop-on-full-inventory", true);
    }

    private Sound parseSound(String name) {
        try {
            return Sound.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Sound.ENTITY_PLAYER_LEVELUP;
        }
    }

    public boolean canMint(int amount) {
        return ledger.size() + amount <= maxMinted;
    }

    public int getRemainingCapacity() {
        return Math.max(0, maxMinted - ledger.size());
    }

    public List<LedgerEntry> mint(Player target, int amount, @Nullable String mintedBy) {
        List<LedgerEntry> minted = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            if (!canMint(1)) {
                break;
            }
            LedgerEntry entry = mintSingle(target, mintedBy == null ? target.getName() : mintedBy);
            if (entry != null) {
                minted.add(entry);
            }
        }
        if (!minted.isEmpty()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getLogger().info("Minted " + minted.size() + " crystal(s) for " + target.getName()));
        }
        return minted;
    }

    private LedgerEntry mintSingle(Player target, String mintedBy) {
        UUID id = UUID.randomUUID();
        ItemStack crystal = createCrystalItem(id);
        ItemStack toGive = crystal.clone();
        Inventory inventory = target.getInventory();
        Map<Integer, ItemStack> leftover = inventory.addItem(toGive);
        boolean delivered = leftover.isEmpty();
        if (!delivered && dropOnFullInventory) {
            for (ItemStack item : leftover.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), item);
            }
            delivered = true;
        }
        if (!delivered) {
            target.sendMessage("§cYour inventory is full. Crystal mint cancelled.");
            return null;
        }
        if (mintSound != null) {
            target.playSound(target.getLocation(), mintSound, 1f, 1f);
        }
        LedgerEntry entry = new LedgerEntry(id, target.getUniqueId(), target.getName(), mintedBy, Instant.now(), true, crystal);
        ledger.addEntry(entry);
        CrystalMintEvent event = new CrystalMintEvent(target, entry, crystal.clone());
        Bukkit.getPluginManager().callEvent(event);
        return entry;
    }

    public ItemStack createCrystalItem(UUID id) {
        ItemStack item = new ItemStack(org.bukkit.Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(idKey, PersistentDataType.STRING, id.toString());
            container.set(markerKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isMinted(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(markerKey, PersistentDataType.BYTE) && container.has(idKey, PersistentDataType.STRING);
    }

    public @Nullable UUID getCrystalId(ItemStack item) {
        if (!isMinted(item)) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String value = container.get(idKey, PersistentDataType.STRING);
        return value != null ? UUID.fromString(value) : null;
    }

    public AmethystLedger getLedger() {
        return ledger;
    }

    public NamespacedKey getIdKey() {
        return idKey;
    }

    public NamespacedKey getMarkerKey() {
        return markerKey;
    }

    public void setMaxMinted(int maxMinted) {
        this.maxMinted = Math.max(0, maxMinted);
        plugin.getConfig().set("amethyst.max-minted", this.maxMinted);
        plugin.saveConfig();
    }

    public int getMaxMinted() {
        return maxMinted;
    }
}
