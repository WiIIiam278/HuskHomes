package net.william278.huskhomes.random;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Represents an engine for generating random position targets
 */
public abstract class RandomTeleportEngine {

    protected final HuskHomes plugin;
    public final String name;
    public long maxAttempts = 12;

    /**
     * Constructor for a random teleport engine
     *
     * @param plugin The HuskHomes plugin instance
     * @param name   The name of the implementing random teleport engine
     */
    protected RandomTeleportEngine(@NotNull HuskHomes plugin, @NotNull String name) {
        this.plugin = plugin;
        this.name = name;
    }

    @NotNull
    public final String getName() {
        return name;
    }

    /**
     * Get the origin position (spawn) of this server
     *
     * @return The origin position
     */
    @NotNull
    protected Position getCenterPoint(@NotNull World world) {
        return plugin.getLocalCachedSpawn()
                .map(s -> s.getPosition(plugin.getServerName()))
                .orElse(Position.at(0d, 128d, 0d, 0f, 0f,
                        world, plugin.getServerName()));
    }

    /**
     * Gets a random position in the {@link World}, or {@link Optional#empty()} if no position could be found in
     * the configured number of attempts
     *
     * @param world The world to find a random position in
     * @param args  The arguments to pass to the random teleport engine
     * @return The position, optionally, which will be empty if the random teleport engine timed out after a
     * {@link #maxAttempts configured number of attempts}
     */
    public abstract CompletableFuture<Optional<Position>> getRandomPosition(@NotNull World world, @NotNull String[] args);

}
