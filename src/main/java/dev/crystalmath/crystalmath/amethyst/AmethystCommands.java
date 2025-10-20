package dev.crystalmath.crystalmath.amethyst;

import dev.crystalmath.crystalmath.beacon.BeaconBridgeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AmethystCommands implements CommandExecutor, TabCompleter {
    private final AmethystService service;
    private final BeaconBridgeManager beaconBridgeManager;

    public AmethystCommands(AmethystService service, BeaconBridgeManager beaconBridgeManager) {
        this.service = service;
        this.beaconBridgeManager = beaconBridgeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendInfo(sender);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "mint" -> handleMint(sender, args);
            case "cap" -> handleCap(sender, args);
            case "info" -> sendInfo(sender);
            case "validate" -> handleValidate(sender);
            default -> sender.sendMessage("§cUnknown subcommand.");
        }
        return true;
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage("§d--- Amethyst Ledger ---");
        sender.sendMessage("§7Minted: §f" + service.getLedger().size());
        sender.sendMessage("§7Cap: §f" + service.getMaxMinted());
        sender.sendMessage("§7Remaining: §f" + service.getRemainingCapacity());
    }

    private void handleMint(CommandSender sender, String[] args) {
        if (!sender.hasPermission("crystalmath.amethyst")) {
            sender.sendMessage("§cYou do not have permission.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /amethyst mint <player> [amount]");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found or offline.");
            return;
        }
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Integer.parseInt(args[2]));
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid amount.");
                return;
            }
        }
        if (!service.canMint(amount)) {
            sender.sendMessage("§cCannot mint that many crystals. Cap reached.");
            return;
        }
        service.mint(target, amount, sender.getName());
        beaconBridgeManager.reloadRecipeAsync();
    }

    private void handleCap(CommandSender sender, String[] args) {
        if (!sender.hasPermission("crystalmath.amethyst")) {
            sender.sendMessage("§cYou do not have permission.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /amethyst cap <newCap>");
            return;
        }
        try {
            int newCap = Math.max(0, Integer.parseInt(args[1]));
            service.setMaxMinted(newCap);
            sender.sendMessage("§aMint cap updated to " + newCap + ".");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number.");
        }
    }

    private void handleValidate(CommandSender sender) {
        if (!sender.hasPermission("crystalmath.amethyst")) {
            sender.sendMessage("§cYou do not have permission.");
            return;
        }
        List<LedgerEntry> removed = service.getLedger().removeInvalidEntries();
        if (removed.isEmpty()) {
            sender.sendMessage("§aLedger is clean. No phantom entries found.");
        } else {
            sender.sendMessage("§eRemoved " + removed.size() + " phantom ledger entr" + (removed.size() == 1 ? "y" : "ies") + ".");
        }
        beaconBridgeManager.reloadRecipeAsync();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("info", "mint", "cap", "validate");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("mint")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
