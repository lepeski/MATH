package dev.crystalmath.crystalmath.claimer;

import dev.crystalmath.crystalmath.CrystalMathPlugin;
import dev.crystalmath.crystalmath.amethyst.AmethystLedger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class GeodeClaimCommand implements CommandExecutor, TabCompleter {
    private final CrystalMathPlugin plugin;
    private final AmethystLedger ledger;
    private final GeodeSpawner spawner;

    public GeodeClaimCommand(CrystalMathPlugin plugin, AmethystLedger ledger) {
        this.plugin = plugin;
        this.ledger = ledger;
        this.spawner = new GeodeSpawner(plugin.getConfig());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("crystalmath.geode")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /geodeclaim <spawn|list|remove>");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "spawn" -> handleSpawn(sender, args);
            case "list" -> handleList(sender);
            case "remove" -> handleRemove(sender, args);
            default -> sender.sendMessage("§cUnknown subcommand.");
        }
        return true;
    }

    private void handleSpawn(CommandSender sender, String[] args) {
        World world;
        int chunkX;
        int chunkZ;
        if (sender instanceof Player player) {
            Chunk chunk = player.getLocation().getChunk();
            world = chunk.getWorld();
            chunkX = chunk.getX();
            chunkZ = chunk.getZ();
            if (args.length >= 3) {
                world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    sender.sendMessage("§cWorld not found.");
                    return;
                }
                try {
                    chunkX = Integer.parseInt(args[2]);
                    chunkZ = args.length >= 4 ? Integer.parseInt(args[3]) : chunkZ;
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid chunk coordinates.");
                    return;
                }
            }
        } else {
            if (args.length < 4) {
                sender.sendMessage("§cUsage: /geodeclaim spawn <world> <chunkX> <chunkZ>");
                return;
            }
            world = Bukkit.getWorld(args[1]);
            if (world == null) {
                sender.sendMessage("§cWorld not found.");
                return;
            }
            try {
                chunkX = Integer.parseInt(args[2]);
                chunkZ = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid chunk coordinates.");
                return;
            }
        }
        if (world == null) {
            sender.sendMessage("§cWorld not available.");
            return;
        }
        GeodeClaim existing = ledger.getGeodeClaim(world.getName(), chunkX, chunkZ);
        if (existing != null) {
            sender.sendMessage("§cA geode claim already exists for this chunk.");
            return;
        }
        int blockX = (chunkX << 4) + 8;
        int blockZ = (chunkZ << 4) + 8;
        int blockY = world.getHighestBlockYAt(blockX, blockZ) + 2;
        Location center = new Location(world, blockX, blockY, blockZ);
        spawner.spawn(world, center);
        GeodeClaim claim = new GeodeClaim(UUID.randomUUID(), world.getName(), chunkX, chunkZ, sender.getName(), Instant.now());
        ledger.addGeodeClaim(claim);
        sender.sendMessage("§aGeode spawned and chunk recorded.");
        plugin.getLogger().info("Geode claim created at " + world.getName() + " " + chunkX + "," + chunkZ + " by " + sender.getName());
    }

    private void handleList(CommandSender sender) {
        if (ledger.getGeodeClaims().isEmpty()) {
            sender.sendMessage("§7No geode claims recorded.");
            return;
        }
        sender.sendMessage("§d--- Geode Claims ---");
        ledger.getGeodeClaims().forEach(claim -> sender.sendMessage("§7" + claim.getId() + " §f" + claim.getWorldName() + " @ " + claim.getChunkX() + "," + claim.getChunkZ()));
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /geodeclaim remove <claimId>");
            return;
        }
        try {
            UUID id = UUID.fromString(args[1]);
            GeodeClaim claim = ledger.getGeodeClaim(id);
            if (claim == null) {
                sender.sendMessage("§cClaim not found.");
                return;
            }
            ledger.removeGeodeClaim(id);
            sender.sendMessage("§eGeode claim removed.");
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid UUID.");
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("spawn", "list", "remove");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            List<String> ids = new ArrayList<>();
            ledger.getGeodeClaims().forEach(claim -> ids.add(claim.getId().toString()));
            return ids;
        }
        return Collections.emptyList();
    }
}
