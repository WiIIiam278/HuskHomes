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

import net.william278.huskhomes.SpongeHuskHomes;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public interface SpongeTaskRunner extends TaskRunner {

    @Override
    default void runAsync(@NotNull Runnable runnable) {
        getPlugin().getGame().asyncScheduler()
                .submit(Task.builder()
                        .plugin(getPlugin().getPluginContainer())
                        .execute(wrap(runnable))
                        .build());
    }

    @Override
    default <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        getPlugin().getGame().asyncScheduler()
                .submit(Task.builder()
                        .plugin(getPlugin().getPluginContainer())
                        .execute(() -> future.complete(supplier.get()))
                        .build());

        return future;
    }

    @Override
    default void runSync(@NotNull Runnable runnable) {
        getPlugin().getGame().server().scheduler()
                .submit(Task.builder()
                        .plugin(getPlugin().getPluginContainer())
                        .execute(runnable)
                        .build());
    }

    @Override
    default int runAsyncRepeating(@NotNull Runnable runnable, long delay) {
        final int taskId = getNextTaskId();
        final CancellableRunnable task = wrap(runnable);
        getTasks().put(taskId, task);
        getPlugin().getGame().asyncScheduler()
                .submit(Task.builder()
                        .plugin(getPlugin().getPluginContainer())
                        .execute(task)
                        .interval(Ticks.of(delay))
                        .build());

        return taskId;
    }

    @Override
    default void runLater(@NotNull Runnable runnable, long delay) {
        getPlugin().getGame().server().scheduler()
                .submit(Task.builder()
                        .plugin(getPlugin().getPluginContainer())
                        .execute(runnable)
                        .delay(Ticks.of(delay))
                        .build());
    }

    @Override
    default void cancelTask(int taskId) {
        if (getTasks().containsKey(taskId)) {
            getTasks().get(taskId).cancel();
            getTasks().remove(taskId);
        }
    }

    @Override
    default void cancelAllTasks() {
        getTasks().values().forEach(CancellableRunnable::cancel);
        getTasks().clear();
    }

    @NotNull
    private CancellableRunnable wrap(@NotNull Runnable runnable) {
        return new CancellableRunnable(runnable);
    }

    @NotNull
    ConcurrentHashMap<Integer, CancellableRunnable> getTasks();

    private int getNextTaskId() {
        return getTasks().keySet().stream().max(Integer::compareTo).orElse(0) + 1;
    }

    @NotNull
    @Override
    SpongeHuskHomes getPlugin();

    /**
     * A wrapper for a {@link Runnable} that can be canceled
     */
    class CancellableRunnable implements Runnable {

        private final Runnable runnable;
        private boolean cancelled = false;

        CancellableRunnable(@NotNull Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            if (cancelled) {
                return;
            }
            runnable.run();
        }

        public void cancel() {
            cancelled = true;
        }

    }
}
