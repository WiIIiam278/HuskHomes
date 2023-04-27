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

import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.command.PaperCommand;
import net.william278.huskhomes.hook.Pl3xMapHook;
import net.william278.huskhomes.util.PaperTaskRunner;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PaperHuskHomes extends BukkitHuskHomes {

    private final static String FOLIA_SCHEDULER_CLASS_NAME = "io.papermc.paper.threadedregions.scheduler.RegionScheduler";

    final PaperTaskRunner paperTaskRunner = new PaperTaskRunner((PaperHuskHomes) getPlugin());

    private final boolean folia = isFolia(); // Always throw ClassNotFoundException not good

    public static boolean isFolia() {
        try {
            Class.forName(FOLIA_SCHEDULER_CLASS_NAME);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

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
        if (folia) {
            return paperTaskRunner.runAsync(runnable); // For Folia
        } else {
            return super.runAsync(runnable); // For Paper and forks
        }
    }

    @Override
    public <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier) {
        if (folia) {
            return paperTaskRunner.supplyAsync(supplier);
        } else {
            return super.supplyAsync(supplier);
        }
    }

    @Override
    public void runSync(@NotNull Runnable runnable) {
        if (folia) {
            paperTaskRunner.runSync(runnable);
        } else {
            super.runSync(runnable);
        }
    }

    @Override
    public int runAsyncRepeating(@NotNull Runnable runnable, long period) {
        if (folia) {
            return paperTaskRunner.runAsyncRepeating(runnable, period);
        } else {
            return super.runAsyncRepeating(runnable, period);
        }
    }

    @Override
    public void runLater(@NotNull Runnable runnable, long delay) {
        if (folia) {
            paperTaskRunner.runLater(runnable, delay);
        } else {
            super.runLater(runnable, delay);
        }
    }

    @Override
    public void cancelTask(int taskId) {
        if (folia) {
            paperTaskRunner.cancelTask(taskId);
        } else {
            super.cancelTask(taskId);
        }
    }

    @Override
    public void cancelAllTasks() {
        if (folia) {
            paperTaskRunner.cancelAllTasks();
        } else {
            super.cancelAllTasks();
        }
    }
}
