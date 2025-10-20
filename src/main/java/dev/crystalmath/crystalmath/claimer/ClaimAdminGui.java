package dev.crystalmath.crystalmath.claimer;

import dev.crystalmath.crystalmath.CrystalMathPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ClaimAdminGui implements Listener {
    private final CrystalMathPlugin plugin;
    private final ClaimManager manager;
    private final NamespacedKey claimKey;
    private final Component title;
    private final int size;

    public ClaimAdminGui(CrystalMathPlugin plugin, ClaimManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.claimKey = new NamespacedKey(plugin, "claim-gui-id");
        this.size = Math.max(9, Math.min(54, plugin.getConfig().getInt("claimer.admin-gui-size", 54)));
        String titleString = plugin.getConfig().getString("claimer.admin-gui-title", "Claims");
        this.title = Component.text(titleString);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(new Holder(), size, title);
        int slot = 0;
        for (Claim claim : manager.getClaims()) {
            if (slot >= size) {
                break;
            }
            ItemStack icon = new ItemStack(org.bukkit.Material.BEACON);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(claim.getOwnerName(), NamedTextColor.AQUA));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("World: " + claim.getWorldName(), NamedTextColor.GRAY));
                if (claim.getCenter() != null) {
                    lore.add(Component.text("Center: " + claim.getCenter().getBlockX() + ", " + claim.getCenter().getBlockZ(), NamedTextColor.GRAY));
                }
                lore.add(Component.text("Radius: " + claim.getRadius(), NamedTextColor.GRAY));
                lore.add(Component.text("ID: " + claim.getId(), NamedTextColor.DARK_GRAY));
                meta.lore(lore);
                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.set(claimKey, PersistentDataType.STRING, claim.getId().toString());
                icon.setItemMeta(meta);
            }
            inventory.setItem(slot++, icon);
        }
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Holder)) {
            return;
        }
        event.setCancelled(true);
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == org.bukkit.Material.AIR) {
            return;
        }
        ItemMeta meta = current.getItemMeta();
        if (meta == null) {
            return;
        }
        String claimId = meta.getPersistentDataContainer().get(claimKey, PersistentDataType.STRING);
        if (claimId == null) {
            return;
        }
        Claim claim = manager.getClaim(UUID.fromString(claimId));
        if (claim == null) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        player.closeInventory();
        if (claim.getCenter() != null) {
            int heightOffset = plugin.getConfig().getInt("claimer.tp-height", 2);
            player.teleport(claim.getCenter().clone().add(0, heightOffset, 0));
            player.sendMessage("§aTeleported to claim " + claim.getId());
        }
    }

    private static final class Holder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return Bukkit.createInventory(null, 9);
        }
    }
}
