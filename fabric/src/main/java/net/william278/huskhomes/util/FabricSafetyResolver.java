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

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.position.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FabricSafetyResolver extends SafetyResolver {

    @Override
    default CompletableFuture<Optional<Location>> findSafeGroundLocation(@NotNull Location location) {
        final MinecraftServer server = ((FabricHuskHomes) getPlugin()).getMinecraftServer();
        final Identifier worldId = Identifier.tryParse(location.getWorld().getName());

        // Ensure the location is on a valid world
        final Optional<ServerWorld> locationWorld = server.getWorldRegistryKeys().stream()
                .filter(key -> key.getValue().equals(worldId)).findFirst()
                .map(server::getWorld);
        if (locationWorld.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        // Ensure the location is within the world border
        final ServerWorld world = locationWorld.get();
        if (!world.getWorldBorder().contains(location.getX(), location.getZ())) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return CompletableFuture.completedFuture(findSafeLocationNear(location, world));
    }

    /**
     * Search for a safe ground location near the given location.
     *
     * @param location The location to search around
     * @return An optional safe location, within 4 blocks of the given location
     */
    private Optional<Location> findSafeLocationNear(@NotNull Location location, @NotNull ServerWorld world) {
        final BlockPos.Mutable blockPos = new BlockPos.Mutable(location.getX(), location.getY(), location.getZ());
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                blockPos.set(location.getX() + x, location.getY(), location.getZ() + z);
                final int highestY = getHighestYAt(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
                final BlockState blockState = world.getBlockState(blockPos.withY(highestY));

                final Material material = blockState.getMaterial();
                final Identifier blockId = Registry.BLOCK.getId(blockState.getBlock());
                if (!material.isLiquid() && material != Material.FIRE && isBlockSafe(blockId.toString())) {
                    return Optional.of(Location.at(
                            blockPos.getX() + 0.5,
                            highestY + 1,
                            blockPos.getZ() + 0.5,
                            location.getWorld()
                    ));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Get the highest Y value at the given X and Z coordinates.
     *
     * @param blockView The block view to search
     * @param x         The X coordinate
     * @param y         The Y coordinate
     * @param z         The Z coordinate
     * @return The highest Y value at the given X and Z coordinates
     */
    private int getHighestYAt(@NotNull BlockView blockView, int x, int y, int z) {
        final BlockPos.Mutable cursor = new BlockPos.Mutable(x, y, z);
        while (blockView.getBlockState(cursor).isAir() && cursor.getY() > blockView.getBottomY()) {
            cursor.move(Direction.DOWN);
        }
        return cursor.getY();
    }

}
