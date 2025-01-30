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
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.position.Location;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface BukkitSavePositionProvider extends SavePositionProvider {

    @Override
    default CompletableFuture<Optional<Location>> findSafeGroundLocation(@NotNull Location location) {
        final org.bukkit.Location bukkitLocation = BukkitHuskHomes.Adapter.adapt(location);
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
                        getMinHeight(bukkitLocation.getWorld()),
                        getMaxHeight(bukkitLocation.getWorld())
                ));
    }

    /**
     * Search for a safe ground location near the given location.
     *
     * @param location The location to search around
     * @param chunk    The chunk snapshot to search
     * @param minY     The minimum Y value of the world
     * @param maxY     The maximum Y value of the world
     * @return An optional safe location, within 4 blocks of the given location
     */
    private Optional<Location> findSafeLocationNear(@NotNull Location location, @NotNull ChunkSnapshot chunk,
                                                    int minY, int maxY) {
        final int chunkX = ((int) location.getX()) & 0xF;
        final int chunkZ = ((int) location.getZ()) & 0xF;

        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                final int x = chunkX + dx;
                final int z = chunkZ + dz;
                if (x < 0 || x >= 16 || z < 0 || z >= 16) {
                    continue;
                }
                final int y = Math.max((minY + 1), Math.min(chunk.getHighestBlockYAt(x, z), maxY)) + 1;
                final Material blockType = chunk.getBlockType(x, y - 1, z);
                final Material bodyBlockType = chunk.getBlockType(x, y, z);
                final Material headBlockType = chunk.getBlockType(x, y + 1, z);
                if (isBlockSafeForStanding(blockType.getKey().toString())
                        && isBlockSafeForOccupation(bodyBlockType.getKey().toString())
                        && isBlockSafeForOccupation(headBlockType.getKey().toString())) {
                    double locx = Math.floor(location.getX()) + dx;
                    if (locx < 0) {
                        locx += 1.5d;
                    } else {
                        locx = locx + 0.5d;
                    }
                    double locz = Math.floor(location.getZ()) + dz;
                    if (locz < 0) {
                        locz += 1.5d;
                    } else {
                        locz = locz + 0.5d;
                    }
                    return Optional.of(Location.at(
                            locx,
                            y,
                            locz,
                            location.getWorld()
                    ));
                }
            }
        }
        return Optional.empty();
    }

    private int getMinHeight(World world) {
        int minHeight = world.getMinHeight();
        for (String pair : getPlugin().getSettings().getRtp().getMinHeight()) {
            String worldName = pair.split(":")[0];
            int settingsHeight = Integer.parseInt(pair.split(":")[1]);
            if (world.getName().equals(worldName) & settingsHeight >= minHeight) {
                minHeight = settingsHeight;
            }
        }
        return minHeight;
    }

    private int getMaxHeight(World world) {
        int maxHeight = world.getMaxHeight();
        for (String pair : getPlugin().getSettings().getRtp().getMaxHeight()) {
            String worldName = pair.split(":")[0];
            int settingsHeight = Integer.parseInt(pair.split(":")[1]);
            if (world.getName().equals(worldName) & settingsHeight >= maxHeight) {
                maxHeight = settingsHeight;
            }
        }
        return maxHeight;
    }

    

}
