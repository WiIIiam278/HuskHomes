package net.william278.huskhomes.random;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * A random teleport engine that uses a Gaussian normal distribution to generate random positions.
 */
public final class NormalDistributionEngine extends RandomTeleportEngine {

    private final int radius;
    private final int spawnRadius;
    private final float mean;
    private final float standardDeviation;

    public NormalDistributionEngine(@NotNull HuskHomes plugin) {
        super(plugin, "Normal Distribution");
        this.radius = plugin.getSettings().getRtpRadius();
        this.spawnRadius = plugin.getSettings().getRtpSpawnRadius();
        this.mean = plugin.getSettings().getRtpDistributionMean();
        this.standardDeviation = plugin.getSettings().getRtpDistributionStandardDeviation();
    }

    /**
     * Generate a {@link Location} through a randomized normally-distributed radius and random angle using the mean and
     * standard deviation, about the origin position.
     *
     * @param origin The origin position
     * @return A generated location
     */
    @NotNull
    public static Location generateLocation(@NotNull Location origin, float mean, float standardDeviation,
                                            float spawnRadius, float maxRadius) {
        // Generate random values
        final float radius = getDistributedRadius(mean, standardDeviation, spawnRadius, maxRadius);
        final float angle = getRandomAngle();

        // Calculate corresponding x and z
        final float z = (float) (radius * Math.cos(angle));
        final float x = (float) (radius * Math.sin(angle));

        return Location.at(
                Math.round(origin.getX()) + x,
                128d,
                Math.round(origin.getZ()) + z,
                origin.getWorld()
        );
    }

    /**
     * Generate a safe ground-level {@link Location} through a randomized normally-distributed radius and random angle
     *
     * @param world The world to generate the location in
     * @return A generated location
     */
    private CompletableFuture<Optional<Location>> generateSafeLocation(@NotNull World world) {
        return plugin.resolveSafeGroundLocation(generateLocation(
                getCenterPoint(world), mean, standardDeviation, spawnRadius, radius));
    }

    /**
     * Generates a normally distributed radius between the spawnRadius and the maximum radius value,
     * using the provided standard deviation and mean.
     *
     * @return the generated radius
     */
    private static float getDistributedRadius(float mean, float standardDeviation,
                                              float spawnRadius, float maxRadius) {
        double value = (new Random().nextGaussian() * mean + standardDeviation) * maxRadius;
        if (value < spawnRadius || value > maxRadius) {
            return getDistributedRadius(mean, standardDeviation, spawnRadius, maxRadius);
        }
        return (float) value;
    }

    /**
     * Generates a random angle in the range [0, 360]
     *
     * @return a random angle in the range [0, 360]
     */
    private static float getRandomAngle() {
        return (float) (Math.random() * 360);
    }

    @Override
    public CompletableFuture<Optional<Position>> getRandomPosition(@NotNull World world, @NotNull String[] args) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Location> location = generateSafeLocation(world).join();
            int attempts = 0;
            while (location.isEmpty()) {
                location = generateSafeLocation(world).join();
                if (attempts >= maxAttempts) {
                    return Optional.empty();
                }
                attempts++;
            }
            return location.map(resolved -> Position.at(resolved, plugin.getServerName()));
        });
    }
}
