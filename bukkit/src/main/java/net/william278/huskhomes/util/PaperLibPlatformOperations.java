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
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Default platform operations using PaperLib for Spigot and legacy Bukkit compatibility.
 */
public class PaperLibPlatformOperations implements PlatformOperations {

    private final BukkitHuskHomes plugin;

    public PaperLibPlatformOperations(@NotNull BukkitHuskHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public void teleport(@NotNull Player player, @NotNull Location location,
                         @NotNull PlayerTeleportEvent.TeleportCause cause, boolean preferAsync) {
        if (preferAsync || plugin.getScheduler().isUsingFolia()) {
            PaperLib.teleportAsync(player, location, cause);
            return;
        }
        player.teleport(location, cause);
    }

    @Override
    public boolean supportsSyncTeleport() {
        return true;
    }

    @Override
    @NotNull
    public CompletableFuture<Chunk> getChunkAtAsync(@NotNull Location location) {
        return PaperLib.getChunkAtAsync(location);
    }

}
