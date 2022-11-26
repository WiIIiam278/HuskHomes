package net.william278.huskhomes.random;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents an engine for generating random position targets
 */
public abstract class RandomTeleportEngine {

    @NotNull
    protected final HuskHomes plugin;

    /**
     * The name of the random teleport engine
     */
    @NotNull
    public final String name;

    /**
     * How many attempts to allow {@link #getRandomPosition} lookups before timing out
     */
    public long randomTimeout = 8;

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
     * Gets a random position in the {@link World}, or {@link Optional#empty()} if no position could be found in
     * the configured number of attempts
     *
     * @param world The world to find a random position in
     * @param args  The arguments to pass to the random teleport engine
     * @return The position, optionally, which will be empty if the random teleport engine timed out after a
     * {@link #randomTimeout configured number of attempts}
     * @implNote This is run asynchronously (i.e. not on the main server thread)
     */
    protected abstract Optional<Position> generatePosition(@NotNull World world, @NotNull String[] args);

    /**
     * Gets a random position in the {@link World}, supplying a future to be completed with the optional position
     * is found, or empty if the operation times out after a number of attempts.
     *
     * @param world The world to find a random position in
     * @param args  The arguments to pass to the random teleport engine
     * @return A {@link CompletableFuture} containing the random position, if one is found in
     * {@link #randomTimeout configured number of attempts}, or if the operation times out after 10 seconds
     */
    public CompletableFuture<Optional<Position>> getRandomPosition(@NotNull World world, @NotNull String[] args) {
        return CompletableFuture.supplyAsync(() -> generatePosition(world, args))
                .orTimeout(10, TimeUnit.SECONDS)
                .exceptionally(e -> Optional.empty());
    }

}
