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
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public final class BukkitPaperCompat {

    private static final Method TELEPORT_ASYNC;
    private static final Method GET_CHUNK_AT_ASYNC;

    static {
        Method teleportAsync = null;
        Method getChunkAtAsync = null;
        try {
            teleportAsync = Player.class.getMethod(
                    "teleportAsync", org.bukkit.Location.class, PlayerTeleportEvent.TeleportCause.class
            );
        } catch (NoSuchMethodException ignored) {
        }
        try {
            getChunkAtAsync = World.class.getMethod("getChunkAtAsync", org.bukkit.Location.class);
        } catch (NoSuchMethodException ignored) {
        }
        TELEPORT_ASYNC = teleportAsync;
        GET_CHUNK_AT_ASYNC = getChunkAtAsync;
    }

    private BukkitPaperCompat() {
    }

    public static void teleportAsync(@NotNull Player player, @NotNull org.bukkit.Location location) {
        if (TELEPORT_ASYNC != null) {
            try {
                TELEPORT_ASYNC.invoke(player, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                return;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to invoke teleportAsync", e);
            }
        }
        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static CompletableFuture<Chunk> getChunkAtAsync(@NotNull HuskHomes plugin, @NotNull World world,
                                                         @NotNull org.bukkit.Location location) {
        if (GET_CHUNK_AT_ASYNC != null) {
            try {
                return (CompletableFuture<Chunk>) GET_CHUNK_AT_ASYNC.invoke(world, location);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to invoke getChunkAtAsync", e);
            }
        }
        final CompletableFuture<Chunk> future = new CompletableFuture<>();
        plugin.runSync(() -> future.complete(world.getChunkAt(location)));
        return future;
    }

}
