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

import net.william278.huskhomes.FabricHuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

public interface FabricTaskRunner extends TaskRunner {

    @Override
    default void runAsync(@NotNull Runnable runnable) {
        CompletableFuture.runAsync(runnable, getPlugin().getMinecraftServer());
    }

    @Override
    default <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, getPlugin().getMinecraftServer());
    }

    @Override
    default void runSync(@NotNull Runnable runnable) {
        getPlugin().getMinecraftServer().executeSync(runnable);
    }

    @NotNull
    @Override
    default UUID runAsyncRepeating(@NotNull Runnable runnable, long delay) {
        final UUID taskId = UUID.randomUUID();
        final CompletableFuture<?> future = new CompletableFuture<>();

        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (future.isCancelled()) {
                executor.shutdown();
                return;
            }
            runnable.run();
        }, 0, delay * 50, TimeUnit.MILLISECONDS);

        getTasks().put(taskId, future);
        return taskId;
    }

    @Override
    default void runLater(@NotNull Runnable runnable, long delay) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delay * 50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            runnable.run();
        }, getPlugin().getMinecraftServer());
    }

    @Override
    default void cancelTask(@NotNull UUID taskId) {
        getTasks().computeIfPresent(taskId, (uuid, completableFuture) -> {
            completableFuture.cancel(true);
            return null;
        });
        getTasks().remove(taskId);
    }

    @Override
    default void cancelAllTasks() {
        getTasks().values().forEach(task -> task.cancel(true));
        getTasks().clear();
    }

    @NotNull
    @Override
    ConcurrentHashMap<UUID, CompletableFuture<?>> getTasks();

    @Override
    @NotNull
    FabricHuskHomes getPlugin();

}
