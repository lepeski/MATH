package dev.crystalmath.crystalmath.claimer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class ClaimListener implements Listener {
    private final ClaimManager manager;

    public ClaimListener(ClaimManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBeaconPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (block.getType() != Material.BEACON) {
            protect(event);
            return;
        }
        Player player = event.getPlayer();
        Location location = block.getLocation();
        if (manager.isOverlap(location)) {
            player.sendMessage("§cThis area is already claimed.");
            event.setCancelled(true);
            return;
        }
        Claim claim = manager.createClaim(player.getUniqueId(), player.getName(), location);
        if (claim == null) {
            player.sendMessage("§cUnable to record claim.");
            return;
        }
        if (manager.shouldShowMessages()) {
            player.sendMessage("§aClaim created with radius §f" + claim.getRadius() + "§a.");
        }
    }

    private void protect(BlockPlaceEvent event) {
        Claim claim = manager.getClaimAt(event.getBlock().getLocation());
        if (claim == null) {
            return;
        }
        Player player = event.getPlayer();
        if (player.hasPermission("crystalmath.claims.admin")) {
            return;
        }
        if (claim.getOwner() != null && claim.getOwner().equals(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage("§cYou cannot modify blocks in this claim.");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Claim claim = manager.getClaimAt(event.getBlock().getLocation());
        if (claim == null) {
            return;
        }
        Player player = event.getPlayer();
        boolean isOwner = claim.getOwner() != null && claim.getOwner().equals(player.getUniqueId());
        boolean isAdmin = player.hasPermission("crystalmath.claims.admin");
        if (event.getBlock().getType() == Material.BEACON) {
            if (!isOwner && !isAdmin) {
                event.setCancelled(true);
                player.sendMessage("§cOnly the owner may remove this beacon.");
                return;
            }
            manager.removeClaim(claim.getId());
            if (manager.shouldShowMessages()) {
                player.sendMessage("§eClaim removed.");
            }
            return;
        }
        if (isOwner || isAdmin) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage("§cYou cannot break blocks in this claim.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        Claim claim = manager.getClaimAt(event.getClickedBlock().getLocation());
        if (claim == null) {
            return;
        }
        Player player = event.getPlayer();
        if (claim.getOwner() != null && claim.getOwner().equals(player.getUniqueId())) {
            return;
        }
        if (player.hasPermission("crystalmath.claims.admin")) {
            return;
        }
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK, LEFT_CLICK_BLOCK -> {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot interact within this claim.");
            }
            default -> {
            }
        }
    }
}
