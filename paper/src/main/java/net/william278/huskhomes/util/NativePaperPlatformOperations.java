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

import net.william278.huskhomes.BukkitHuskHomes;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Platform operations using native Paper/Folia APIs.
 */
public class NativePaperPlatformOperations implements PlatformOperations {

    private final BukkitHuskHomes plugin;

    public NativePaperPlatformOperations(@NotNull BukkitHuskHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public void teleport(@NotNull Player player, @NotNull Location location,
                         @NotNull PlayerTeleportEvent.TeleportCause cause, boolean preferAsync) {
        player.teleportAsync(location, cause)
                .exceptionally(throwable -> {
                    plugin.log(Level.WARNING,
                            String.format("Failed to teleport player %s asynchronously", player.getName()),
                            throwable);
                    return false;
                })
                .thenAccept(success -> {
                    if (!success) {
                        plugin.log(Level.WARNING,
                                String.format("Asynchronous teleport for player %s completed unsuccessfully",
                                        player.getName()));
                    }
                });
    }

    @Override
    public boolean supportsSyncTeleport() {
        return false;
    }

    @Override
    @NotNull
    public CompletableFuture<Chunk> getChunkAtAsync(@NotNull Location location) {
        final World world = location.getWorld();
        if (world == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Location world is null"));
        }
        return world.getChunkAtAsync(location, true);
    }

}
