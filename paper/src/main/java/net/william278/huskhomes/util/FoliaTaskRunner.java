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

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.position.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static net.william278.huskhomes.PaperHuskHomes.folia;
import static org.bukkit.Bukkit.getServer;

public interface FoliaTaskRunner extends TaskRunner {

    default RegionScheduler getRegionScheduler() {
        if (folia) {
            return getServer().getRegionScheduler();
        } else {
            return null;
        }
    }

    default GlobalRegionScheduler getGlobalRegionScheduler() {
        if (folia) {
            return getServer().getGlobalRegionScheduler();
        } else {
            return null;
        }
    }

    ArrayList<ScheduledTask> scheduledTasks = new ArrayList<>();

    default int runAsync(@NotNull Runnable runnable, Location location) {
        if (getGlobalRegionScheduler() != null) {
            // For folia
            if (location != null) {
                getRegionScheduler().run((BukkitHuskHomes) getPlugin(), new org.bukkit.Location(getServer().getWorld(location.getWorld().getUuid()), location.getX(), location.getY(), location.getZ()), scheduledTask -> {
                    runnable.run();
                    scheduledTasks.add(scheduledTask);
                });
                // For paper
            } else {
                getGlobalRegionScheduler().run((BukkitHuskHomes) getPlugin(), scheduledTask -> {
                    runnable.run();
                    scheduledTasks.add(scheduledTask);
                });
            }
            return scheduledTasks.size() - 1;
        }
        return this.runAsync(runnable);
    }

    default <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier, Location location) {
        if (getGlobalRegionScheduler() != null) {
            final CompletableFuture<T> future = new CompletableFuture<>();
            if (location != null) {
                getRegionScheduler().execute((BukkitHuskHomes) getPlugin(), new org.bukkit.Location(getServer().getWorld(location.getWorld().getUuid()), location.getX(), location.getY(), location.getZ()), () -> future.complete(supplier.get()));
            } else {
                getGlobalRegionScheduler().execute((BukkitHuskHomes) getPlugin(), () -> future.complete(supplier.get()));
            }
            return future;
        }
        return this.supplyAsync(supplier);
    }


    default void runSync(@NotNull Runnable runnable, Location location) {
        if (getGlobalRegionScheduler() != null) {
            if (location != null) {
                getRegionScheduler().execute((BukkitHuskHomes) getPlugin(), new org.bukkit.Location(getServer().getWorld(location.getWorld().getUuid()), location.getX(), location.getY(), location.getZ()), runnable);
            } else {
                getGlobalRegionScheduler().execute((BukkitHuskHomes) getPlugin(), runnable);
            }
            return;
        }
        this.runSync(runnable);
    }

    default int runAsyncRepeating(@NotNull Runnable runnable, long period, Location location) {
        if (getGlobalRegionScheduler() != null) {
            if (location != null) {
                scheduledTasks.add(getRegionScheduler().runAtFixedRate((BukkitHuskHomes) getPlugin(), new org.bukkit.Location(getServer().getWorld(location.getWorld().getUuid()), location.getX(), location.getY(), location.getZ()), scheduledTask -> runnable.run(), 1, period));
            } else {
                scheduledTasks.add(getGlobalRegionScheduler().runAtFixedRate((BukkitHuskHomes) getPlugin(), scheduledTask -> runnable.run(), 1, period));
            }
            return scheduledTasks.size() - 1;
        }
        return this.runAsyncRepeating(runnable, period);
    }

    default void runLater(@NotNull Runnable runnable, long delay, Location location) {
        if (getGlobalRegionScheduler() != null) {
            if (location != null) {
                getRegionScheduler().runDelayed((BukkitHuskHomes) getPlugin(), new org.bukkit.Location(getServer().getWorld(location.getWorld().getUuid()), location.getX(), location.getY(), location.getZ()), scheduledTask -> runnable.run(), delay);
            } else {
                getGlobalRegionScheduler().runDelayed((BukkitHuskHomes) getPlugin(), scheduledTask -> runnable.run(), delay);
            }
            return;
        }
        this.runLater(runnable, delay);
    }
}
