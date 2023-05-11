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

import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.scheduling.GracefulScheduling;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public interface BukkitTaskRunner extends TaskRunner {

    @Override
    default void runAsync(@NotNull Runnable runnable) {
        final int taskId = getTasks().size();
        getTasks().put(taskId, getScheduler().asyncScheduler().run(runnable));
    }

    @Override
    default <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        getScheduler().asyncScheduler().run(() -> future.complete(supplier.get()));
        return future;
    }

    @Override
    default void runSync(@NotNull Runnable runnable) {
        getScheduler().globalRegionalScheduler().run(runnable);
    }

    @Override
    default int runAsyncRepeating(@NotNull Runnable runnable, long period) {
        final int taskId = getTasks().size();
        getTasks().put(taskId, getScheduler().asyncScheduler().runAtFixedRate(
                runnable,
                Duration.ZERO,
                getDurationTicks(period)
        ));
        return taskId;
    }

    @Override
    default void runLater(@NotNull Runnable runnable, long delay) {
        getScheduler().asyncScheduler().runDelayed(runnable, getDurationTicks(delay));
    }

    @Override
    default void cancelTask(int taskId) {
        if (getTasks().containsKey(taskId)) {
            getTasks().get(taskId).cancel();
        }
    }

    @Override
    default void cancelAllTasks() {
        getTasks().values().forEach(ScheduledTask::cancel);
        getTasks().clear();
        getScheduler().cancelGlobalTasks();
    }

    @NotNull
    GracefulScheduling getScheduler();

    @NotNull
    ConcurrentHashMap<Integer, ScheduledTask> getTasks();

    @NotNull
    default Duration getDurationTicks(long ticks) {
        return Duration.of(ticks * 50, ChronoUnit.MILLIS);
    }

}
