package net.william278.huskhomes.random;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class RandomTeleportEngine {

    @NotNull
    protected final HuskHomes plugin;

    @NotNull
    public final String name;

    /**
     * How many attempts to allow {@link #getRandomPosition} lookups before timing out
     */
    public long randomTimeout = 8;

    protected RandomTeleportEngine(@NotNull HuskHomes plugin, @NotNull String name) {
        this.plugin = plugin;
        this.name = name;
    }

    /**
     * Get the origin position (spawn) of this server
     *
     * @return The origin position
     */
    protected final Position getOrigin(@NotNull World world) {
        return plugin.getLocalCachedSpawn()
                .flatMap(spawn -> {
                    if (!spawn.worldUuid.equals(world.uuid.toString())) {
                        return Optional.empty();
                    }
                    return spawn.getPosition(plugin.getPluginServer());
                })
                .orElse(new Position(0d, 128d, 0d, 0f, 0f,
                        world, plugin.getPluginServer()));
    }

    /**
     * Gets a random position in the {@link World}, supplying a future to be completed with the optional position
     * is found, or empty if the operation times out
     *
     * @param world The world to find a random position in
     * @param args  The arguments to pass to the random teleport engine
     * @return A {@link CompletableFuture} containing the random position, if one was found before the
     * {@link #randomTimeout operation timed out}
     */
    public abstract CompletableFuture<Optional<Position>> getRandomPosition(@NotNull World world, @NotNull String[] args);

}
