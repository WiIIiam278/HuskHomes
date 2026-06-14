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

import net.minecraft.server.MinecraftServer;
//#if MC>=260102
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
//import net.minecraft.world.level.block.BlockView;
//#else
//$$ import net.minecraft.block.Block;
//$$ import net.minecraft.block.FireBlock;
//$$ import net.minecraft.block.FluidBlock;
//$$ import net.minecraft.registry.Registries;
//$$ import net.minecraft.server.world.ServerWorld;
//$$ import net.minecraft.util.Identifier;
//$$ import net.minecraft.util.math.BlockPos;
//$$ import net.minecraft.util.math.Direction;
//$$ import net.minecraft.world.BlockView;
//#endif
import net.minecraft.world.level.storage.ServerLevelData;
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

public interface FabricSavePositionProvider extends SavePositionProvider {

    @Override
    default CompletableFuture<Optional<Location>> findSafeGroundLocation(@NotNull Location location) {
        final MinecraftServer server = ((FabricHuskHomes) getPlugin()).getMinecraftServer();

        // Ensure the location is on a valid world
        //#if MC>=260102
        final String worldName = location.getWorld().getName();
        final Optional<ServerLevel> locationWorld = StreamSupport.stream(server.getAllLevels().spliterator(), false)
                .filter(level -> ((ServerLevelData) level.getLevelData()).getLevelName().equals(worldName))
                .findFirst();
        //#else
        //$$ final Identifier worldId = Identifier.tryParse(location.getWorld().getName());
        //$$ final Optional<ServerWorld> locationWorld = server.getWorldRegistryKeys().stream()
        //$$        .filter(key -> key.getValue().equals(worldId)).findFirst()
        //$$        .map(server::getWorld);
        //#endif
        if (locationWorld.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        // Ensure the location is within the world border
        //#if MC>=260102
        final ServerLevel world = locationWorld.get();
        if (!world.getWorldBorder().isWithinBounds(location.getX(), location.getZ())) {
        //#else
        //$$ final ServerWorld world = locationWorld.get();
        //$$ if (!world.getWorldBorder().contains(location.getX(), location.getZ())) {
        //#endif
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return CompletableFuture.completedFuture(findSafeLocationNear(location, world, location.getWorld().getName()));
    }

    /**
     * Search for a safe ground location near the given location.
     *
     * @param location The location to search around
     * @return An optional safe location, within 4 blocks of the given location
     */
    private Optional<Location> findSafeLocationNear(
            @NotNull Location location,
            //#if MC>=260102
            @NotNull ServerLevel world,
            //#else
            //$$ @NotNull ServerWorld world,
            //#endif
            @NotNull String worldName
    ) {
        //#if MC>=260102
        final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(location.getX(), location.getY(), location.getZ());
        //#else
        //$$ final BlockPos.Mutable blockPos = new BlockPos.Mutable(location.getX(), location.getY(), location.getZ());
        //#endif
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                blockPos.set(location.getX() + x, location.getY(), location.getZ() + z);
                final Optional<Integer> y = getY(
                        location,
                        blockPos,
                        world,
                        getMinHeight(world, worldName),
                        getMaxHeight(world, worldName)
                );

                if (y.isPresent()) {
                    double locx = blockPos.getX();
                    if (locx < 0) {
                        locx += 1.5d;
                    } else {
                        locx = locx + 0.5d;
                    }
                    double locz = blockPos.getZ();
                    if (locz < 0) {
                        locz += 1.5d;
                    } else {
                        locz = locz + 0.5d;
                    }
                    return Optional.of(Location.at(
                            locx,
                            y.get(),
                            locz,
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
     private int getHighestYAt(
             //#if MC>=260102
             @NotNull ServerLevel blockView,
             //#else
             //$$ @NotNull BlockView blockView,
             //#endif
             int x, int y, int z) {
         //#if MC>=260102
         final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y, z);
         final int minY = blockView.getMinY();
        //#else
        //$$ final BlockPos.Mutable cursor = new BlockPos.Mutable(x, y, z);
        //$$ final int minY = blockView.getBottomY();
        //#endif
        while (blockView.getBlockState(cursor).isAir() && cursor.getY() > minY) {
            cursor.move(Direction.DOWN);
        }
        return cursor.getY();
    }

    private Identifier getBlockId(
            //#if MC>=260102
            @NotNull ServerLevel world,
            //#else
            //$$ @NotNull ServerWorld world,
            //#endif
            @NotNull BlockPos pos, int y) {
        //#if MC>=260102
        final Block block = world.getBlockState(pos.atY(y - 1)).getBlock();
        return BuiltInRegistries.BLOCK.getKey(block);
        //#else
        //$$ final Block block = world.getBlockState(pos.withY(y - 1)).getBlock();
        //$$ return Registries.BLOCK.getId(block);
        //#endif
    }

    private boolean isSafeLocation(
            //#if MC>=260102
            @NotNull ServerLevel world,
            //#else
            //$$ @NotNull ServerWorld world,
            //#endif
            @NotNull BlockPos blockPos, int y) {
        //#if MC>=260102
        final Block block = world.getBlockState(blockPos.atY(y - 1)).getBlock();
        //#else
        //$$ final Block block = world.getBlockState(blockPos.withY(y - 1)).getBlock();
        //#endif
        final Identifier id = getBlockId(world, blockPos, y - 1);
        final Identifier bodyBlockId = getBlockId(world, blockPos, y);
        final Identifier headBlockId = getBlockId(world, blockPos, y + 1);
        //#if MC>=260102
        final boolean isFluidBlock = block instanceof LiquidBlock;
        //#else
        //$$ final boolean isFluidBlock = block instanceof FluidBlock;
        //#endif

        return !isFluidBlock && !(block instanceof FireBlock)
                && isBlockSafeForStanding(id.toString()) && isBlockSafeForOccupation(bodyBlockId.toString())
                && isBlockSafeForOccupation(headBlockId.toString());
    }

    private Optional<Integer> getY(@NotNull Location location, @NotNull BlockPos blockPos,
                                   //#if MC>=260102
                                   @NotNull ServerLevel world,
                                   //#else
                                   //$$ @NotNull ServerWorld world,
                                   //#endif
                                   int minY, int maxY) {
        final int highestY = getHighestYAt(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (location.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            for (int y = minY + 1; y < Math.min(highestY, maxY); y++) {
                if (isSafeLocation(world, blockPos, y)) {
                    return Optional.of(y);
                }
            }
            return Optional.empty();
        } else {
            final String worldName = location.getWorld().getName();
            final int y = Math.max(getMinHeight(world, worldName), Math.min(highestY + 1, getMaxHeight(world, worldName)));
            return isSafeLocation(world, blockPos, y) ? Optional.of(y) : Optional.empty();
        }
    }

    private int getMinHeight(
            //#if MC>=260102
            ServerLevel world,
            //#else
            //$$ ServerWorld world,
            //#endif
            String worldName) {
        //#if MC>=260102
        int minHeight = world.getMinY();
        //#else
        //$$ int minHeight = world.getDimension().minY();
        //#endif
        for (String pair : getPlugin().getSettings().getRtp().getMinHeight()) {
            String settingsWorldName = pair.split(":")[0];
            int settingsHeight = Integer.parseInt(pair.split(":")[1]);
            if (settingsWorldName.equals(worldName) & settingsHeight >= minHeight) {
                minHeight = settingsHeight;
            }
        }
        return minHeight;
    }

    private int getMaxHeight(
            //#if MC>=260102
            ServerLevel world,
            //#else
            //$$ ServerWorld world,
            //#endif
            String worldName) {
        //#if MC>=260102
        int maxHeight = world.getHeight() + world.getMinY();
        //#else
        //$$ int maxHeight = world.getDimension().height() + world.getDimension().minY();
        //#endif
        for (String pair : getPlugin().getSettings().getRtp().getMaxHeight()) {
            String settingsWorldName = pair.split(":")[0];
            int settingsHeight = Integer.parseInt(pair.split(":")[1]);
            if (settingsWorldName.equals(worldName) & settingsHeight >= maxHeight) {
                maxHeight = settingsHeight;
            }
        }
        return maxHeight;
    }

}
