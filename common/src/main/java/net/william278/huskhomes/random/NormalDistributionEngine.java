package net.william278.huskhomes.random;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * A random teleport engine that uses a Gaussian normal distribution to generate random positions.
 */
public class NormalDistributionEngine extends RandomTeleportEngine {

    protected final int radius;
    protected final int spawnRadius;
    private final float mean;
    private final float standardDeviation;
    private final int cacheSize;

    // Cache of random positions
    @NotNull
    private final Map<UUID, LinkedList<Position>> cache;

    public NormalDistributionEngine(@NotNull HuskHomes implementor) {
        super(implementor, "Normal Distribution");
        this.radius = implementor.getSettings().rtpRadius;
        this.spawnRadius = implementor.getSettings().rtpSpawnRadius;
        this.mean = implementor.getSettings().rtpDistributionMean;
        this.standardDeviation = implementor.getSettings().rtpDistributionStandardDeviation;
        this.cacheSize = implementor.getSettings().rtpLocationCacheSize;
        this.cache = new HashMap<>();

        // Prepare cache
        for (final World world : implementor.getWorlds()) {
            if (implementor.getSettings().rtpRestrictedWorlds.contains(world.name)) {
                continue;
            }
            cache.put(world.uuid, new LinkedList<>());
        }
    }

    /**
     * Generate a {@link Location} through a randomized normally-distributed radius and random angle using the mean and
     * standard deviation, about the origin position.
     *
     * @param origin The origin position
     * @return A generated location
     */
    @NotNull
    private static Location generateLocation(@NotNull Location origin, float mean, float standardDeviation,
                                             float spawnRadius, float maxRadius) {
        // Generate random values
        final float radius = generateNormallyDistributedRadius(mean, standardDeviation, spawnRadius, maxRadius);
        final float angle = generateRandomAngle();

        // Calculate corresponding x and z
        final float z = (float) (radius * Math.cos(angle));
        final float x = (float) (radius * Math.sin(angle));

        return new Location(origin.x + x, 128, origin.z + z,
                origin.yaw, origin.pitch, origin.world);
    }

    /**
     * Generate a safe ground-level {@link Location} through a randomized normally-distributed radius and random angle
     *
     * @param world The world to generate the location in
     * @return A generated location
     */
    private CompletableFuture<Optional<Location>> generateSafeLocation(@NotNull World world) {
        return plugin.resolveSafeGroundLocation(generateLocation(
                getOrigin(world), mean, standardDeviation, spawnRadius, radius));
    }

    /**
     * Generates a normally distributed radius between the spawnRadius and the maximum radius value,
     * using the provided standard deviation and mean.
     *
     * @return the generated radius
     */
    private static float generateNormallyDistributedRadius(float mean, float standardDeviation,
                                                           float spawnRadius, float maxRadius) {
        double value = (new Random().nextGaussian() * mean + standardDeviation) * maxRadius;
        if (value < spawnRadius || value > maxRadius) {
            return generateNormallyDistributedRadius(mean, standardDeviation, spawnRadius, maxRadius);
        }
        return (float) value;
    }

    /**
     * Generates a random angle in the range [0, 360]
     *
     * @return a random angle in the range [0, 360]
     */
    private static float generateRandomAngle() {
        return (float) (Math.random() * 360);
    }

    /**
     * Populate the cache with a set of random locations
     */
    private void populateCache(@NotNull World world) {
        final Server server = plugin.getPluginServer();
        while (cache.size() < cacheSize) {
            generateSafeLocation(world).thenAccept(location -> location
                    .ifPresent(value -> cache.getOrDefault(world.uuid, new LinkedList<>())
                            .addLast(new Position(value, server))));
        }
    }

    @Override
    public CompletableFuture<Position> findRandomPosition(@NotNull World world, @NotNull String[] args) {
        CompletableFuture<Position> position;
        if (!cache.getOrDefault(world.uuid, new LinkedList<>()).isEmpty()) {
            position = CompletableFuture.completedFuture(cache.get(world.uuid).removeFirst());
        } else {
            position = CompletableFuture.supplyAsync(() -> {
                Optional<Location> resolved = Optional.empty();
                while (resolved.isEmpty()) {
                    resolved = generateSafeLocation(world).join();
                }
                return new Position(resolved.get(), plugin.getPluginServer());
            });
        }

        CompletableFuture.runAsync(() -> populateCache(world));
        return position;
    }
}
