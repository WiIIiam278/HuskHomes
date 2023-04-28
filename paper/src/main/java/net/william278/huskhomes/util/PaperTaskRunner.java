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

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.PaperHuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class PaperTaskRunner implements TaskRunner {

    private final PaperHuskHomes plugin;

    public PaperTaskRunner(PaperHuskHomes plugin) {
        this.plugin = plugin;
    }

    private final AtomicInteger atomicInteger = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, ScheduledTask> scheduledTasks = new ConcurrentHashMap<>();

    public int runAsync(@NotNull Runnable runnable) {
        int id = atomicInteger.getAndIncrement();
        plugin.getServer().getGlobalRegionScheduler().run(plugin, scheduledTask -> {
            runnable.run();
            scheduledTasks.put(id, scheduledTask);
        });
        return id;
    }

    public  <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        PaperHuskHomes plugin = (PaperHuskHomes) getPlugin();
        plugin.getServer().getGlobalRegionScheduler().run(plugin, scheduledTask -> future.complete(supplier.get()));
        return future;
    }

    public void runSync(@NotNull Runnable runnable) {
        PaperHuskHomes plugin = (PaperHuskHomes) getPlugin();
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, runnable);
    }

    public int runAsyncRepeating(@NotNull Runnable runnable, long period) {
        int id = atomicInteger.getAndIncrement();
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            runnable.run();
            scheduledTasks.put(id, scheduledTask);
        }, 1, period);
        return id;
    }

    public void runLater(@NotNull Runnable runnable, long delay) {
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> runnable.run(), delay);
    }

    public void cancelTask(int taskId) {
        scheduledTasks.get(taskId).cancel();
        scheduledTasks.remove(taskId);
    }

    public void cancelAllTasks() {
        plugin.getServer().getGlobalRegionScheduler().cancelTasks(plugin);
    }

    @NotNull
    @Override
    public HuskHomes getPlugin() {
        return plugin;
    }
}
