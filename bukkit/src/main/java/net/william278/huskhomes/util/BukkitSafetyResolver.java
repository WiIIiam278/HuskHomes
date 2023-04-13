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

        return PaperLib.getChunkAtAsync(bukkitLocation).thenApply(Chunk::getChunkSnapshot).thenApply(snapshot -> {
            final int chunkX = bukkitLocation.getBlockX() & 0xF;
            final int chunkZ = bukkitLocation.getBlockZ() & 0xF;

            for (int dX = -1; dX <= 2; dX++) {
                for (int dZ = -1; dZ <= 2; dZ++) {
                    final int x = chunkX + dX;
                    final int z = chunkZ + dZ;
                    if (x < 0 || x >= 16 || z < 0 || z >= 16) {
                        continue;
                    }
                    final int y = snapshot.getHighestBlockYAt(x, z);
                    final Material blockType = snapshot.getBlockType(chunkX, y, chunkZ);
                    if (isBlockSafe(blockType.getKey().toString())) {
                        return Optional.of(Location.at(
                                (location.getX() + dX) + 0.5d,
                                y + 1.25d,
                                (location.getZ() + dZ) + 0.5d,
                                location.getWorld()
                        ));
                    }
                }
            }
            return Optional.empty();
        });
    }

}
