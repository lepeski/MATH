package dev.crystalmath.crystalmath.amethyst;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CrystalMintEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player target;
    private final LedgerEntry entry;
    private final ItemStack item;

    public CrystalMintEvent(@NotNull Player target, @NotNull LedgerEntry entry, @NotNull ItemStack item) {
        super(true);
        this.target = target;
        this.entry = entry;
        this.item = item;
    }

    public Player getTarget() {
        return target;
    }

    public LedgerEntry getEntry() {
        return entry;
    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
