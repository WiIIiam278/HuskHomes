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
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Represents an engine for generating random position targets.
 */
public abstract class RandomTeleportEngine {

    protected final HuskHomes plugin;
    public final String name;
    public long maxAttempts = 12;

    /**
     * Constructor for a random teleport engine.
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
     * Get the origin (center) position for the world.
     *
     * <p>Note that this will return the {@link HuskHomes#getServerSpawn() server spawn position} if it is in the same
     * world as the world passed to this method, otherwise it will return a position at 0, 128, 0 on the provided world.
     *
     * @param world The world to get the origin position for
     * @return The origin position
     */
    @NotNull
    protected Position getCenterPoint(@NotNull World world) {
        return plugin.getServerSpawn()
                .map(spawn -> spawn.getPosition(plugin.getServerName()))
                .flatMap(position -> {
                    if (position.getWorld().equals(world)) {
                        return Optional.of(position);
                    }
                    return Optional.empty();
                })
                .orElse(Position.at(0d, 128d, 0d, world, plugin.getServerName()));
    }

    /**
     * Gets a random position in the {@link World}, or {@link Optional#empty()} if no position could be found in
     * the configured number of attempts.
     *
     * @param world The world to find a random position in
     * @param args  The arguments to pass to the random teleport engine
     * @return An Optional position, if one could be found in the {@link #maxAttempts max attempts}
     */
    public abstract CompletableFuture<Optional<Position>> getRandomPosition(@NotNull World world,
                                                                            @NotNull String[] args);

}
