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

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Platform-specific Bukkit operations abstracting Paper/Folia native APIs from Spigot fallbacks.
 */
public interface PlatformOperations {

    /**
     * Teleport a player. Native Paper/Folia implementations always use {@code teleportAsync}
     * regardless of {@code preferAsync}.
     *
     * @param player      the player to teleport
     * @param location    the destination
     * @param cause       the teleport cause
     * @param preferAsync whether asynchronous teleportation is preferred
     */
    void teleport(@NotNull Player player, @NotNull Location location,
                  @NotNull PlayerTeleportEvent.TeleportCause cause, boolean preferAsync);

    /**
     * Whether synchronous {@link Player#teleport(Location)} is safe on this platform.
     *
     * @return {@code false} on Folia and other platforms that require async teleports
     */
    boolean supportsSyncTeleport();

    /**
     * Load a chunk asynchronously at the given location.
     *
     * @param location the location to load the chunk at
     * @return a future completing with the loaded chunk
     */
    @NotNull
    CompletableFuture<Chunk> getChunkAtAsync(@NotNull Location location);

}
