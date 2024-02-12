/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.random;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * A random teleport engine that uses a Gaussian normal distribution to generate random positions.
 */
public final class NormalDistributionEngine extends RandomTeleportEngine {

    private final Settings.RtpSettings.RtpRadius radius;
    private final float mean;
    private final float standardDeviation;

    public NormalDistributionEngine(@NotNull HuskHomes plugin) {
        super(plugin, "Normal Distribution");
        this.radius = plugin.getSettings().getRtp().getRegion();
        this.mean = plugin.getSettings().getRtp().getDistributionMean();
        this.standardDeviation = plugin.getSettings().getRtp().getDistributionStandardDeviation();
    }

    // Utility for determining a valid spawn radius
    private static int determineSpawnRadius(int radius, int spawnRadius, @NotNull HuskHomes plugin) {
        if (spawnRadius >= radius) {
            plugin.log(Level.WARNING, "The RTP spawn radius is greater than or equal to the RTP radius. "
                    + "This will result in the RTP engine being unable to find a suitable location to teleport to. "
                    + "Please set the RTP spawn radius to a value less than the RTP radius.");
            return radius - 1;
        }
        return spawnRadius;
    }

    /**
     * Generate a {@link Location} through a randomized normally distributed radius and random angle using the mean and
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
     * Generate a safe ground-level {@link Location} through a randomized normally-distributed radius and random angle.
     *
     * @param world The world to generate the location in
     * @return A generated location
     */
    private CompletableFuture<Optional<Location>> generateSafeLocation(@NotNull World world) {
        return plugin.findSafeGroundLocation(generateLocation(
                getCenterPoint(world), mean, standardDeviation,
                radius.getMin(), radius.getMax()
        ));
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
     * Generates a random angle in the range {@code [0, 360]}.
     *
     * @return a random angle in the range {@code [0, 360]}
     */
    private static float getRandomAngle() {
        return (float) (Math.random() * 360);
    }

    @Override
    public CompletableFuture<Optional<Position>> getRandomPosition(@NotNull World world, @NotNull String[] args) {
        return plugin.supplyAsync(() -> {
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
