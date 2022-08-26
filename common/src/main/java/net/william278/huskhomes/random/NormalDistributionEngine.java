package net.william278.huskhomes.random;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A random teleport engine that uses a Gaussian normal distribution to generate random positions.
 */
public class NormalDistributionEngine extends RandomTeleportEngine {

    private final HuskHomes plugin;
    private final float mean;
    private final float standardDeviation;

    public NormalDistributionEngine(@NotNull HuskHomes implementor) {
        super(implementor, "Normal Distribution");
        this.mean = implementor.getSettings().rtpDistributionMean;
        this.standardDeviation = implementor.getSettings().rtpDistributionStandardDeviation;
        this.plugin = implementor;
    }

    @Override
    protected @NotNull CompletableFuture<Optional<Location>> generateRandomPosition(@NotNull Location origin) {
        return CompletableFuture.supplyAsync(() -> plugin.getSafeGroundLocation(generateLocation(origin, mean,
                        standardDeviation, spawnRadius, radius)).join())
                .orTimeout(5, TimeUnit.SECONDS)
                .exceptionally(throwable -> Optional.empty());
    }

    /**
     * Generate a location through a randomized normally-distributed radius and random angle using the mean and
     * standard deviation, about the origin position.
     *
     * @param origin The origin position
     * @return A generated location
     */
    protected static Location generateLocation(@NotNull Location origin, float mean, float standardDeviation,
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
}
