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

package net.william278.huskhomes;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.command.PaperCommand;
import net.william278.huskhomes.hook.Pl3xMapHook;
import net.william278.huskhomes.position.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PaperHuskHomes extends BukkitHuskHomes {

    ArrayList<ScheduledTask> scheduledTasks = new ArrayList<>();

    @Override
    public void registerHooks() {
        super.registerHooks();

        if (getMapHook().isEmpty() && isDependencyLoaded("Pl3xMap")) {
            getHooks().add(new Pl3xMapHook(this));
        }
    }

    @NotNull
    @Override
    public List<Command> registerCommands() {
        return Arrays.stream(BukkitCommand.Type.values())
                .map(type -> {
                    final Command command = type.getCommand();
                    if (!getSettings().isCommandDisabled(command)) {
                        new PaperCommand(command, this).register();
                        return command;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public int runAsync(@NotNull Runnable runnable, Location location) {
        if (getGlobalRegionScheduler() != null) {
            if (location != null) {
                getRegionScheduler().run(this, new org.bukkit.Location(getServer().getWorld(location.getWorld().getUuid()), location.getX(), location.getY(), location.getZ()), scheduledTask -> {
                    runnable.run();
                    scheduledTasks.add(scheduledTask);
                });
            } else {
                getGlobalRegionScheduler().run(this, scheduledTask -> {
                    runnable.run();
                    scheduledTasks.add(scheduledTask);
                });
            }
            return scheduledTasks.size() - 1;
        }
        return super.runAsync(runnable, location);
    }

    @Override
    public <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier, Location location) {
        if (getGlobalRegionScheduler() != null) {
            final CompletableFuture<T> future = new CompletableFuture<>();
            if (location != null) {
                getRegionScheduler().execute(this, new org.bukkit.Location(getServer().getWorld(location.getWorld().getUuid()), location.getX(), location.getY(), location.getZ()), () -> future.complete(supplier.get()));
            } else {
                getGlobalRegionScheduler().execute(this, () -> future.complete(supplier.get()));
            }
            return future;
        }
        return super.supplyAsync(supplier, location);
    }

    @Override
    public void runSync(@NotNull Runnable runnable, Location location) {
        if (getGlobalRegionScheduler() != null) {
            if (location != null) {
                getRegionScheduler().execute(this, new org.bukkit.Location(getServer().getWorld(location.getWorld().getUuid()), location.getX(), location.getY(), location.getZ()), runnable);
            } else {
                getGlobalRegionScheduler().execute(this, runnable);
            }
            return;
        }
        super.runSync(runnable, location);
    }

    @Override
    public int runAsyncRepeating(@NotNull Runnable runnable, long period, Location location) {
        if (getGlobalRegionScheduler() != null) {
            if (location != null) {
                scheduledTasks.add(getRegionScheduler().runAtFixedRate(this, new org.bukkit.Location(getServer().getWorld(location.getWorld().getUuid()), location.getX(), location.getY(), location.getZ()), scheduledTask -> runnable.run(), 1, period));
            } else {
                scheduledTasks.add(getGlobalRegionScheduler().runAtFixedRate(this, scheduledTask -> runnable.run(), 1, period));
            }
            return scheduledTasks.size() - 1;
        }
        return super.runAsyncRepeating(runnable, period, location);
    }

    @Override
    public void runLater(@NotNull Runnable runnable, long delay, Location location) {
        if (getGlobalRegionScheduler() != null) {
            if (location != null) {
                getRegionScheduler().runDelayed(this, new org.bukkit.Location(getServer().getWorld(location.getWorld().getUuid()), location.getX(), location.getY(), location.getZ()), scheduledTask -> runnable.run(), delay);
            } else {
                getGlobalRegionScheduler().runDelayed(this, scheduledTask -> runnable.run(), delay);
            }
            return;
        }
        super.runLater(runnable, delay, location);
    }

    @Override
    public void cancelTask(int taskId) {
        if (getGlobalRegionScheduler() != null) {
            scheduledTasks.get(taskId).cancel();
            scheduledTasks.remove(taskId);
            return;
        }
        super.cancelTask(taskId);
    }

    @Override
    public void cancelAllTasks() {
        if (getGlobalRegionScheduler() != null) {
            getGlobalRegionScheduler().cancelTasks(this);
            return;
        }
        super.cancelAllTasks();
    }
}
