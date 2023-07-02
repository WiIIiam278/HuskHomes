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

package net.william278.huskhomes.util;

import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.World;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Static class providing utility methods for adapting Bukkit objects to HuskHomes and vice versa.
 */
public final class BukkitAdapter {

    /**
     * Adapt a Bukkit {@link org.bukkit.Location} to a HuskHomes {@link Location}.
     *
     * @param location the Bukkit {@link org.bukkit.Location} to adapt
     * @return the adapted {@link Location}
     */
    public static Optional<org.bukkit.Location> adaptLocation(@NotNull Location location) {
        org.bukkit.World world = Bukkit.getWorld(location.getWorld().getName());
        if (world == null) {
            world = Bukkit.getWorld(location.getWorld().getUuid());
        }
        if (world == null) {
            world = Bukkit.getWorlds().stream()
                    .filter(w -> w.getEnvironment().name().equalsIgnoreCase(
                            location.getWorld().getEnvironment().name())).findFirst()
                    .orElse(null);
        }
        if (world == null) {
            return Optional.empty();
        }
        return Optional.of(new org.bukkit.Location(
                world,
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        ));
    }

    /**
     * Adapt a HuskHomes {@link org.bukkit.Location} to a Bukkit {@link org.bukkit.Location}.
     *
     * @param location the HuskHomes {@link org.bukkit.Location} to adapt
     * @return the adapted {@link org.bukkit.Location}
     */
    public static Optional<Location> adaptLocation(@NotNull org.bukkit.Location location) {
        if (location.getWorld() == null) {
            return Optional.empty();
        }
        return Optional.of(Location.at(location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch(),
                adaptWorld(location.getWorld()).orElse(new net.william278.huskhomes.position.World())));
    }

    public static Optional<World> adaptWorld(@Nullable org.bukkit.World world) {
        if (world == null) {
            return Optional.empty();
        }
        return Optional.of(World.from(
                world.getName(), world.getUID(),
                switch (world.getEnvironment()) {
                    case NORMAL -> World.Environment.OVERWORLD;
                    case NETHER -> World.Environment.NETHER;
                    case THE_END -> World.Environment.THE_END;
                    default -> World.Environment.CUSTOM;
                })
        );
    }

}
