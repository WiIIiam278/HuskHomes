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
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public interface BukkitTaskRunner extends TaskRunner {

    @Override
    default int runAsync(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously((BukkitHuskHomes) getPlugin(), runnable).getTaskId();
    }

    @Override
    default <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously((BukkitHuskHomes) getPlugin(),
                () -> future.complete(supplier.get()));
        return future;
    }

    @Override
    default void runSync(@NotNull Runnable runnable) {
        Bukkit.getScheduler().runTask((BukkitHuskHomes) getPlugin(), runnable).getTaskId();
    }

    @Override
    default int runAsyncRepeating(@NotNull Runnable runnable, long period) {
        AtomicInteger taskId = new AtomicInteger();
        taskId.set(Bukkit.getScheduler().runTaskTimerAsynchronously((BukkitHuskHomes) getPlugin(),
                runnable, 0, period).getTaskId());
        return taskId.get();
    }

    @Override
    default void runLater(@NotNull Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater((BukkitHuskHomes) getPlugin(), runnable, delay).getTaskId();
    }

    @Override
    default void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    @Override
    default void cancelAllTasks() {
        Bukkit.getScheduler().cancelTasks((BukkitHuskHomes) getPlugin());
    }

}
