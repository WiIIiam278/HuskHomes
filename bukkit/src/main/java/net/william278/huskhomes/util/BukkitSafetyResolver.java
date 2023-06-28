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

import io.papermc.lib.PaperLib;
import net.william278.huskhomes.position.Location;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface BukkitSafetyResolver extends SafetyResolver {

    @Override
    default CompletableFuture<Optional<Location>> findSafeGroundLocation(@NotNull Location location) {
        final org.bukkit.Location bukkitLocation = BukkitAdapter.adaptLocation(location).orElse(null);
        if (bukkitLocation == null || bukkitLocation.getWorld() == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        // Ensure the location is within the world border
        if (!bukkitLocation.getWorld().getWorldBorder().isInside(bukkitLocation)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        // Search nearby blocks for a safe location
        return PaperLib.getChunkAtAsync(bukkitLocation)
                .thenApply(Chunk::getChunkSnapshot)
                .thenApply(snapshot -> findSafeLocationNear(
                        location,
                        snapshot,
                        bukkitLocation.getWorld().getMinHeight()
                ));
    }

    /**
     * Search for a safe ground location near the given location
     *
     * @param location The location to search around
     * @param snapshot The chunk snapshot to search
     * @param minY     The minimum Y value of the world
     * @return An optional safe location, within 4 blocks of the given location
     */
    private Optional<Location> findSafeLocationNear(@NotNull Location location, @NotNull ChunkSnapshot snapshot, int minY) {
        final int chunkX = ((int) location.getX()) & 0xF;
        final int chunkZ = ((int) location.getZ()) & 0xF;

        for (int dX = -SEARCH_RADIUS; dX <= SEARCH_RADIUS; dX++) {
            for (int dZ = -SEARCH_RADIUS; dZ <= SEARCH_RADIUS; dZ++) {
                final int x = chunkX + dX;
                final int z = chunkZ + dZ;
                if (x < 0 || x >= 16 || z < 0 || z >= 16) {
                    continue;
                }
                final int y = Math.max((minY + 1), snapshot.getHighestBlockYAt(x, z)) + 1;
                final Material blockType = snapshot.getBlockType(x, y - 1, z);
                final Material bodyBlockType = snapshot.getBlockType(x, y, z);
                final Material headBlockType = snapshot.getBlockType(x, y + 1, z);
                if (isBlockSafeForStanding(blockType.getKey().toString())
                    && isBlockSafeForOccupation(bodyBlockType.getKey().toString())
                    && isBlockSafeForOccupation(headBlockType.getKey().toString())) {
                    return Optional.of(Location.at(
                            (location.getX() + dX) + 0.5d,
                            y,
                            (location.getZ() + dZ) + 0.5d,
                            location.getWorld()
                    ));
                }
            }
        }
        return Optional.empty();
    }

}
