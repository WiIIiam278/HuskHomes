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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldManager;

import java.util.Optional;

public final class SpongeAdapter {

    /**
     * Adapt a Sponge {@link ServerLocation} to a HuskHomes {@link Location}.
     *
     * @param location the Sponge {@link ServerLocation} to adapt
     * @return the adapted {@link Location}
     */
    public static Optional<ServerLocation> adaptLocation(@NotNull Location location) {
        final WorldManager worldManager = Sponge.server().worldManager();
        return worldManager
                .world(ResourceKey.resolve(location.getWorld().getName()))
                .map(world -> ServerLocation.of(world, location.getX(), location.getY(), location.getZ()))
                .or(() -> {
                    final Optional<ResourceKey> worldKey = worldManager.worldKey(location.getWorld().getUuid());
                    return worldKey.flatMap(resourceKey -> worldManager.world(resourceKey)
                            .map(world -> ServerLocation.of(world, location.getX(), location.getY(), location.getZ())));
                });
    }

    /**
     * Adapt a HuskHomes {@link Location} to a Sponge {@link ServerLocation}.
     *
     * @param location the HuskHomes {@link Location} to adapt
     * @return the adapted {@link ServerLocation}
     */
    public static Optional<Location> adaptLocation(@NotNull ServerLocation location) {
        return Optional.of(Location.at(
                location.x(), location.y(), location.z(),
                adaptWorld(location.world()).orElse(new World()))
        );
    }

    /**
     * Adapt a Sponge {@link ServerWorld} to a HuskHomes {@link World}.
     *
     * @param world the Sponge {@link ServerWorld} to adapt
     * @return the adapted {@link World}
     */
    public static Optional<World> adaptWorld(@Nullable ServerWorld world) {
        if (world == null) {
            return Optional.empty();
        }

        // Get the world type from the world properties
        final DefaultedRegistryType<WorldType> registry = WorldTypes.registry().type().asDefaultedType(world::engine);
        return Optional.of(World.from(
                world.key().toString(),
                world.uniqueId(),
                switch (world.properties().worldType().key(registry).asString()) {
                    case "minecraft:overworld" -> World.Environment.OVERWORLD;
                    case "minecraft:the_nether" -> World.Environment.NETHER;
                    case "minecraft:the_end" -> World.Environment.THE_END;
                    default -> World.Environment.CUSTOM;
                }
        ));
    }

}
