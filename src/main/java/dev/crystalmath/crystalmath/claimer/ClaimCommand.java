package dev.crystalmath.crystalmath.claimer;

import dev.crystalmath.crystalmath.CrystalMathPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class ClaimCommand implements CommandExecutor, TabCompleter {
    private final CrystalMathPlugin plugin;
    private final ClaimManager manager;
    private final ClaimAdminGui adminGui;

    public ClaimCommand(CrystalMathPlugin plugin, ClaimManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.adminGui = new ClaimAdminGui(plugin, manager);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (player.hasPermission("crystalmath.claims.admin")) {
                    adminGui.open(player);
                    return true;
                }
                Claim claim = manager.getClaimAt(player.getLocation());
                if (claim == null) {
                    player.sendMessage("§7You are not inside a claim.");
                } else {
                    player.sendMessage("§aClaim owner: §f" + claim.getOwnerName());
                    player.sendMessage("§aRadius: §f" + claim.getRadius());
                }
                return true;
            }
            sender.sendMessage("There are " + manager.getClaims().size() + " claims recorded.");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players may use this.");
                    return true;
                }
                if (!player.hasPermission("crystalmath.claims.admin")) {
                    sender.sendMessage("§cYou do not have permission.");
                    return true;
                }
                adminGui.open(player);
                return true;
            }
            case "tp" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players may use this.");
                    return true;
                }
                if (!player.hasPermission("crystalmath.claims.admin")) {
                    player.sendMessage("§cYou do not have permission.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /claims tp <claimId>");
                    return true;
                }
                Claim claim = getClaim(args[1]);
                if (claim == null || claim.getCenter() == null) {
                    player.sendMessage("§cClaim not found.");
                    return true;
                }
                int heightOffset = plugin.getConfig().getInt("claimer.tp-height", 2);
                player.teleport(claim.getCenter().clone().add(0, heightOffset, 0));
                player.sendMessage("§aTeleported to claim.");
                return true;
            }
            case "remove" -> {
                if (!sender.hasPermission("crystalmath.claims.admin")) {
                    sender.sendMessage("§cYou do not have permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /claims remove <claimId>");
                    return true;
                }
                Claim claim = getClaim(args[1]);
                if (claim == null) {
                    sender.sendMessage("§cClaim not found.");
                    return true;
                }
                manager.removeClaim(claim.getId());
                sender.sendMessage("§eClaim removed.");
                return true;
            }
            case "info" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players may use this.");
                    return true;
                }
                Claim claim = manager.getClaimAt(player.getLocation());
                if (claim == null) {
                    player.sendMessage("§7You are not inside a claim.");
                } else {
                    player.sendMessage("§aClaim owner: §f" + claim.getOwnerName());
                    player.sendMessage("§aRadius: §f" + claim.getRadius());
                }
                return true;
            }
            default -> sender.sendMessage("§cUnknown subcommand.");
        }
        return true;
    }

    private Claim getClaim(String arg) {
        try {
            UUID id = UUID.fromString(arg);
            return manager.getClaim(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("info");
            if (sender.hasPermission("crystalmath.claims.admin")) {
                options.add("list");
                options.add("tp");
                options.add("remove");
            }
            return options;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("remove"))) {
            if (!sender.hasPermission("crystalmath.claims.admin")) {
                return Collections.emptyList();
            }
            List<String> ids = new ArrayList<>();
            for (Claim claim : manager.getClaims()) {
                ids.add(claim.getId().toString());
            }
            return ids;
        }
        return Collections.emptyList();
    }
}
