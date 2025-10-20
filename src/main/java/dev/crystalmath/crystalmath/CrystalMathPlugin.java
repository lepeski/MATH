package dev.crystalmath.crystalmath;

import dev.crystalmath.crystalmath.amethyst.AmethystCommands;
import dev.crystalmath.crystalmath.amethyst.AmethystLedger;
import dev.crystalmath.crystalmath.amethyst.AmethystService;
import dev.crystalmath.crystalmath.beacon.BeaconBridgeManager;
import dev.crystalmath.crystalmath.claimer.ClaimCommand;
import dev.crystalmath.crystalmath.claimer.ClaimListener;
import dev.crystalmath.crystalmath.claimer.ClaimManager;
import dev.crystalmath.crystalmath.claimer.GeodeClaimCommand;
import dev.crystalmath.crystalmath.common.Logging;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class CrystalMathPlugin extends JavaPlugin {

    private AmethystLedger ledger;
    private AmethystService amethystService;
    private BeaconBridgeManager beaconBridgeManager;
    private ClaimManager claimManager;

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            getLogger().warning("Could not create data folder");
        }

        this.ledger = new AmethystLedger(new File(dataFolder, "ledger.yml"));
        this.ledger.load(this);

        this.amethystService = new AmethystService(this, ledger);
        this.amethystService.load();

        this.beaconBridgeManager = new BeaconBridgeManager(this, amethystService, getConfig());
        this.beaconBridgeManager.load();

        this.claimManager = new ClaimManager(this, new File(dataFolder, "claims.yml"));
        this.claimManager.load();

        ClaimListener claimListener = new ClaimListener(claimManager);
        Bukkit.getPluginManager().registerEvents(claimListener, this);

        registerCommands();

        Logging.logConfigured(getConfig(), getLogger(), "amethyst", "AmethystControl system loaded.");
        Logging.logConfigured(getConfig(), getLogger(), "beacon", "BeaconBridge system loaded.");
        Logging.logConfigured(getConfig(), getLogger(), "claimer", "Claimer system loaded.");
        Logging.logConfigured(getConfig(), getLogger(), "geodes", "Geode claiming system ready.");
    }

    @Override
    public void onDisable() {
        if (this.ledger != null) {
            this.ledger.save();
        }
        if (this.claimManager != null) {
            this.claimManager.save();
        }
    }

    private void registerCommands() {
        AmethystCommands amethystCommands = new AmethystCommands(amethystService, beaconBridgeManager);
        if (getCommand("amethyst") != null) {
            getCommand("amethyst").setExecutor(amethystCommands);
            getCommand("amethyst").setTabCompleter(amethystCommands);
        }

        ClaimCommand claimCommand = new ClaimCommand(this, claimManager);
        if (getCommand("claims") != null) {
            getCommand("claims").setExecutor(claimCommand);
            getCommand("claims").setTabCompleter(claimCommand);
        }

        GeodeClaimCommand geodeCommand = new GeodeClaimCommand(this, ledger);
        if (getCommand("geodeclaim") != null) {
            getCommand("geodeclaim").setExecutor(geodeCommand);
            getCommand("geodeclaim").setTabCompleter(geodeCommand);
        }
    }

    public @NotNull AmethystService getAmethystService() {
        return amethystService;
    }

    public @NotNull ClaimManager getClaimManager() {
        return claimManager;
    }

    public @NotNull FileConfiguration getPluginConfig() {
        return getConfig();
    }
}
