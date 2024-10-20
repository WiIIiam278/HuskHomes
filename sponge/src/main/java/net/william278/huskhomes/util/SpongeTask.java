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

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.SpongeHuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.util.Ticks;

import static org.spongepowered.api.scheduler.Task.builder;

public interface SpongeTask extends Task {

    class Sync extends Task.Sync implements SpongeTask {

        private ScheduledTask task;

        protected Sync(@NotNull HuskHomes plugin, @NotNull Runnable runnable,
                       @SuppressWarnings("unused") @Nullable OnlineUser user, long delayTicks) {
            super(plugin, runnable, delayTicks);
        }

        @Override
        public void cancel() {
            if (task != null && !cancelled) {
                task.cancel();
            }
            super.cancel();
        }

        @Override
        public void run() {
            if (!cancelled) {
                final SpongeHuskHomes plugin = ((SpongeHuskHomes) getPlugin());
                this.task = plugin.getGame().server().scheduler().submit(builder()
                        .plugin(plugin.getPluginContainer())
                        .delay(Ticks.of(delayTicks))
                        .execute(runnable)
                        .build());
            }
        }
    }

    class Async extends Task.Async implements SpongeTask {

        private ScheduledTask task;

        protected Async(@NotNull HuskHomes plugin, @NotNull Runnable runnable, long delayTicks) {
            super(plugin, runnable, delayTicks);
        }

        @Override
        public void cancel() {
            if (task != null && !cancelled) {
                task.cancel();
            }
            super.cancel();
        }

        @Override
        public void run() {
            if (!cancelled) {
                final SpongeHuskHomes plugin = ((SpongeHuskHomes) getPlugin());
                this.task = plugin.getGame().asyncScheduler().submit(builder()
                        .plugin(plugin.getPluginContainer())
                        .execute(runnable)
                        .build());
            }
        }
    }

    class Repeating extends Task.Repeating implements SpongeTask {

        private ScheduledTask task;

        protected Repeating(@NotNull HuskHomes plugin, @NotNull Runnable runnable, long repeatingTicks) {
            super(plugin, runnable, repeatingTicks);
        }

        @Override
        public void cancel() {
            if (task != null && !cancelled) {
                task.cancel();
            }
            super.cancel();
        }

        @Override
        public void run() {
            if (!cancelled) {
                final SpongeHuskHomes plugin = ((SpongeHuskHomes) getPlugin());
                this.task = plugin.getGame().asyncScheduler().submit(builder()
                        .plugin(plugin.getPluginContainer())
                        .interval(Ticks.of(repeatingTicks))
                        .execute(runnable)
                        .build());
            }
        }
    }

    interface Supplier extends Task.Supplier {

        @NotNull
        @Override
        default Task.Sync getSyncTask(@NotNull Runnable runnable, @Nullable OnlineUser user, long delayTicks) {
            return new Sync(getPlugin(), runnable, user, delayTicks);
        }

        @NotNull
        @Override
        default Task.Async getAsyncTask(@NotNull Runnable runnable, long delayTicks) {
            return new Async(getPlugin(), runnable, delayTicks);
        }

        @NotNull
        @Override
        default Task.Repeating getRepeatingTask(@NotNull Runnable runnable, long repeatingTicks) {
            return new Repeating(getPlugin(), runnable, repeatingTicks);
        }

        @Override
        default void cancelTasks() {
        }

    }

}
