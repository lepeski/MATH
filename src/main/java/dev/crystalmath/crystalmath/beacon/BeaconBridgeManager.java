package dev.crystalmath.crystalmath.beacon;

import dev.crystalmath.crystalmath.CrystalMathPlugin;
import dev.crystalmath.crystalmath.amethyst.AmethystService;
import dev.crystalmath.crystalmath.amethyst.CrystalMintEvent;
import dev.crystalmath.crystalmath.amethyst.LedgerEntry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.List;

public final class BeaconBridgeManager implements Listener {
    private final CrystalMathPlugin plugin;
    private final AmethystService service;
    private final FileConfiguration config;
    private final NamespacedKey recipeKey;
    private boolean recipeEnabled;

    public BeaconBridgeManager(CrystalMathPlugin plugin, AmethystService service, FileConfiguration config) {
        this.plugin = plugin;
        this.service = service;
        this.config = config;
        this.recipeKey = new NamespacedKey(plugin, "beacon_minted");
    }

    public void load() {
        this.recipeEnabled = config.getBoolean("beacon.recipe-enabled", true);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        reloadRecipeAsync();
    }

    public void reloadRecipeAsync() {
        Bukkit.getScheduler().runTask(plugin, this::reloadRecipe);
    }

    private void reloadRecipe() {
        Bukkit.removeRecipe(recipeKey);
        if (!recipeEnabled) {
            return;
        }
        List<ItemStack> mintedItems = new ArrayList<>();
        for (LedgerEntry entry : service.getLedger().getEntries()) {
            ItemStack item = entry.getCrystal();
            if (item == null || item.getType() != Material.AMETHYST_SHARD) {
                continue;
            }
            item.setAmount(1);
            mintedItems.add(item);
        }
        if (mintedItems.isEmpty()) {
            return;
        }
        ItemStack result = new ItemStack(Material.BEACON);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);
        recipe.shape("GGG", "GCG", "OOO");
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('C', new RecipeChoice.ExactChoice(mintedItems));
        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onCrystalMint(CrystalMintEvent event) {
        reloadRecipeAsync();
    }
}
