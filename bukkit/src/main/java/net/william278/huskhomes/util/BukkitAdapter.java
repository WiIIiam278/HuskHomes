package net.william278.huskhomes.util;

import net.william278.huskhomes.position.Location;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Static class providing utility methods for adapting Bukkit objects to HuskHomes and vice versa
 */
public final class BukkitAdapter {

    /**
     * Adapt a Bukkit {@link org.bukkit.Location} to a HuskHomes {@link Location}
     *
     * @param location the Bukkit {@link org.bukkit.Location} to adapt
     * @return the adapted {@link Location}
     */
    public static Optional<org.bukkit.Location> adaptLocation(@NotNull Location location) {
        org.bukkit.World world = Bukkit.getWorld(location.world.name);
        if (world == null) {
            world = Bukkit.getWorld(location.world.uuid);
        }
        if (world == null) {
            return Optional.empty();
        }
        return Optional.of(new org.bukkit.Location(world, location.x, location.y, location.z,
                location.yaw, location.pitch));
    }

    /**
     * Adapt a HuskHomes {@link org.bukkit.Location} to a Bukkit {@link org.bukkit.Location}
     *
     * @param location the HuskHomes {@link org.bukkit.Location} to adapt
     * @return the adapted {@link org.bukkit.Location}
     */
    public static Optional<Location> adaptLocation(@NotNull org.bukkit.Location location) {
        if (location.getWorld() == null) return Optional.empty();
        return Optional.of(new Location(location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch(),
                adaptWorld(location.getWorld()).orElse(new net.william278.huskhomes.position.World())));
    }

    public static Optional<net.william278.huskhomes.position.World> adaptWorld(@Nullable org.bukkit.World world) {
        if (world == null) {
            return Optional.empty();
        }
        return Optional.of(new net.william278.huskhomes.position.World(world.getName(), world.getUID(),
                switch (world.getEnvironment()) {
                    case NORMAL -> net.william278.huskhomes.position.World.Environment.OVERWORLD;
                    case NETHER -> net.william278.huskhomes.position.World.Environment.NETHER;
                    case THE_END -> net.william278.huskhomes.position.World.Environment.THE_END;
                    case CUSTOM -> net.william278.huskhomes.position.World.Environment.CUSTOM;
                }, adaptLocation(world.getSpawnLocation()).orElse(null)));
    }

}
