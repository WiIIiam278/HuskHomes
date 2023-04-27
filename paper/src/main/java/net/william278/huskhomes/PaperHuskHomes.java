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

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.command.PaperCommand;
import net.william278.huskhomes.hook.Pl3xMapHook;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PaperHuskHomes extends BukkitHuskHomes {

    private final static String FOLIA_SCHEDULER_CLASS_NAME = "io.papermc.paper.threadedregions.scheduler.RegionScheduler";

    public static boolean isFolia() {
        try {
            Class.forName(FOLIA_SCHEDULER_CLASS_NAME);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    boolean folia = PaperHuskHomes.isFolia();

    Optional<GlobalRegionScheduler> getGlobalRegionScheduler() {
        if (!folia) {
            return Optional.empty();
        }
        return Optional.of(getServer().getGlobalRegionScheduler());
    }

    final ArrayList<ScheduledTask> scheduledTasks = new ArrayList<>();

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
    public int runAsync(@NotNull Runnable runnable) {
        if (getGlobalRegionScheduler().isPresent()) {
            getGlobalRegionScheduler().get().run(this, scheduledTasks::add);
            return scheduledTasks.size() - 1;
        } else {
            return super.runAsync(runnable);
        }
    }

    @Override
    public <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        if (getGlobalRegionScheduler().isPresent()) {
            getGlobalRegionScheduler().get().run(this, scheduledTask -> future.complete(supplier.get()));
            return future;
        } else {
            return super.supplyAsync(supplier);
        }
    }

    @Override
    public void runSync(@NotNull Runnable runnable) {
        if (getGlobalRegionScheduler().isPresent()) {
            getGlobalRegionScheduler().get().execute(this, runnable);
        } else {
            super.runSync(runnable);
        }
    }

    @Override
    public int runAsyncRepeating(@NotNull Runnable runnable, long period) {
        if (getGlobalRegionScheduler().isPresent()) {
            getGlobalRegionScheduler().get().runAtFixedRate(this, scheduledTasks::add, 1, period);
            return scheduledTasks.size() - 1;
        } else {
            return super.runAsyncRepeating(runnable, period);
        }
    }

    @Override
    public void runLater(@NotNull Runnable runnable, long delay) {
        if (getGlobalRegionScheduler().isPresent()) {
            getGlobalRegionScheduler().get().runDelayed(this, scheduledTask -> runnable.run(), delay);
        } else {
            super.runLater(runnable, delay);
        }
    }

    @Override
    public void cancelTask(int taskId) {
        if (!folia) {
            super.cancelTask(taskId);
        }
        scheduledTasks.get(taskId).cancel();
    }

    @Override
    public void cancelAllTasks() {
        if (!folia) {
            super.cancelAllTasks();
        }
        getServer().getGlobalRegionScheduler().cancelTasks(this);
    }
}
