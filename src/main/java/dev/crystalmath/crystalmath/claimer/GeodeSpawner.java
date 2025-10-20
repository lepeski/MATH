package dev.crystalmath.crystalmath.claimer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public final class GeodeSpawner {
    private final Material outer;
    private final Material middle;
    private final Material core;
    private final int radius;

    public GeodeSpawner(FileConfiguration config) {
        this.radius = config.getInt("geodes.cluster-radius", 4);
        this.outer = defaulted(Material.matchMaterial(config.getString("geodes.outer-block", "SMOOTH_BASALT")), Material.SMOOTH_BASALT);
        this.middle = defaulted(Material.matchMaterial(config.getString("geodes.middle-block", "CALCITE")), Material.CALCITE);
        this.core = defaulted(Material.matchMaterial(config.getString("geodes.core-block", "AMETHYST_BLOCK")), Material.AMETHYST_BLOCK);
    }

    private Material defaulted(Material value, Material fallback) {
        return value != null ? value : fallback;
    }

    public void spawn(World world, Location center) {
        if (world == null || center == null) {
            return;
        }
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int radiusSquared = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distanceSq = x * x + y * y + z * z;
                    Location target = new Location(world, cx + x, cy + y, cz + z);
                    if (distanceSq >= radiusSquared) {
                        continue;
                    }
                    if (distanceSq > (radius - 1) * (radius - 1)) {
                        if (outer != null) {
                            target.getBlock().setType(outer, false);
                        }
                    } else if (distanceSq > (radius - 2) * (radius - 2)) {
                        if (middle != null) {
                            target.getBlock().setType(middle, false);
                        }
                    } else {
                        if (core != null) {
                            target.getBlock().setType(core, false);
                        }
                    }
                }
            }
        }
    }
}
