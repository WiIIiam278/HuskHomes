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

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SavePositionProvider {

    int SEARCH_RADIUS = 2;

    /**
     * Returns a safe ground location for the specified {@link Location} if possible.
     *
     * @param location the {@link Location} to find a safe ground location for
     * @return a {@link CompletableFuture} with an optional safe ground position, if one could be found
     */
    CompletableFuture<Optional<Location>> findSafeGroundLocation(@NotNull Location location);

    /**
     * Returns if the block, by provided identifier, is unsafe to stand on.
     *
     * @param blockId The block identifier (e.g. {@code minecraft:stone})
     * @return {@code true} if the block is on the unsafe blocks list, {@code false} otherwise
     */
    default boolean isBlockSafeForStanding(@NotNull String blockId) {
        return !getPlugin().getUnsafeBlocks().isUnsafe(blockId);
    }

    /**
     * Returns if the block, by provided identifier, is unsafe to stand in.
     *
     * @param blockId The block identifier (e.g. {@code minecraft:stone})
     * @return {@code true} if the block can be occupied, {@code false} otherwise
     */
    default boolean isBlockSafeForOccupation(@NotNull String blockId) {
        return !getPlugin().getUnsafeBlocks().isUnsafeToOccupy(blockId);
    }

    @NotNull
    HuskHomes getPlugin();

}
