/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes.util;

import net.william278.huskhomes.FandHuskHomes;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public interface FandTask extends Task {

    final class Sync extends Task.Sync implements FandTask {

        private io.fand.api.scheduler.Task task;

        private Sync(@NotNull HuskHomes plugin, @NotNull Runnable runnable, long delayTicks) {
            super(plugin, runnable, delayTicks);
        }

        @Override
        public void run() {
            final var scheduler = ((FandHuskHomes) plugin).getContext().scheduler();
            task = delayTicks <= 0 ? scheduler.runMain(runnable) : scheduler.runMainAfterTicks(runnable, delayTicks);
        }

        @Override
        public void cancel() {
            if (task != null) {
                task.cancel();
            }
            super.cancel();
        }
    }

    final class Async extends Task.Async implements FandTask {

        private io.fand.api.scheduler.Task task;

        private Async(@NotNull HuskHomes plugin, @NotNull Runnable runnable, long delayTicks) {
            super(plugin, runnable, delayTicks);
        }

        @Override
        public void run() {
            final var scheduler = ((FandHuskHomes) plugin).getContext().scheduler();
            task = delayTicks <= 0
                    ? scheduler.runAsync(runnable)
                    : scheduler.runAsyncAfter(runnable, Duration.ofMillis(delayTicks * 50L));
        }

        @Override
        public void cancel() {
            if (task != null) {
                task.cancel();
            }
            super.cancel();
        }
    }

    final class Repeating extends Task.Repeating implements FandTask {

        private io.fand.api.scheduler.Task task;

        private Repeating(@NotNull HuskHomes plugin, @NotNull Runnable runnable, long repeatingTicks) {
            super(plugin, runnable, repeatingTicks);
        }

        @Override
        public void run() {
            task = ((FandHuskHomes) plugin).getContext().scheduler()
                    .runMainRepeatingTicks(runnable, 0L, repeatingTicks);
        }

        @Override
        public void cancel() {
            if (task != null) {
                task.cancel();
            }
            super.cancel();
        }
    }

    interface Supplier extends Task.Supplier {

        @Override
        @NotNull
        default Task.Sync getSyncTask(@NotNull Runnable runnable, @Nullable OnlineUser user, long delayTicks) {
            return new Sync(getPlugin(), runnable, delayTicks);
        }

        @Override
        @NotNull
        default Task.Async getAsyncTask(@NotNull Runnable runnable, long delayTicks) {
            return new Async(getPlugin(), runnable, delayTicks);
        }

        @Override
        @NotNull
        default Task.Repeating getRepeatingTask(@NotNull Runnable runnable, long repeatingTicks) {
            return new Repeating(getPlugin(), runnable, repeatingTicks);
        }

        @Override
        default void cancelTasks() {
        }
    }
}
