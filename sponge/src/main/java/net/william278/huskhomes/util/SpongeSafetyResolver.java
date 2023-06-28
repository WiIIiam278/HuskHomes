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
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SpongeSafetyResolver extends SafetyResolver {

    @Override
    default CompletableFuture<Optional<Location>> findSafeGroundLocation(@NotNull Location location) {
        final Optional<ServerLocation> adaptedLocation = SpongeAdapter.adaptLocation(location);
        if (adaptedLocation.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        // Ensure the location is within the world border
        final ServerLocation serverLocation = adaptedLocation.get();
        if (isInBorder(serverLocation.world().border(), serverLocation.blockPosition())) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return CompletableFuture.completedFuture(findSafeLocationNear(serverLocation, location.getWorld()));
    }

    /**
     * Search for a safe ground location near the given location
     *
     * @param location The location to search around
     * @param world    The world to search in
     * @return An optional safe location, within 4 blocks of the given location
     */
    private Optional<Location> findSafeLocationNear(@NotNull ServerLocation location, @NotNull World world) {
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                final Vector3i cursor = location.asHighestLocation().blockPosition().add(x, -1, z);
                final Vector3i cursorBody = location.asHighestLocation().blockPosition().add(x, 0, z);
                final Vector3i cursorHead = location.asHighestLocation().blockPosition().add(x, 1, z);
                final BlockState blockState = location.world().block(cursor);
                final BlockState bodyBlockState = location.world().block(cursorBody);
                final BlockState headBlockState = location.world().block(cursorHead);
                if (isBlockSafeForStanding(blockState.type().key(RegistryTypes.BLOCK_TYPE).asString())
                    && isBlockSafeForOccupation(bodyBlockState.type().key(RegistryTypes.BLOCK_TYPE).asString())
                    && isBlockSafeForOccupation(headBlockState.type().key(RegistryTypes.BLOCK_TYPE).asString())) {
                    return Optional.of(Location.at(
                            cursor.x() + 0.5,
                            cursor.y(),
                            cursor.z() + 0.5,
                            world
                    ));
                }
            }
        }
        return Optional.empty();
    }

    private boolean isInBorder(WorldBorder border, Vector3i position) {
        final Vector2d center = border.center();
        final double radius = border.diameter() / 2;
        final double x = position.x() - center.x();
        final double z = position.z() - center.y();
        return x * x + z * z < radius * radius;
    }

}