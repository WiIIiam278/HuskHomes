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
    default int runSync(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTask((BukkitHuskHomes) getPlugin(), runnable).getTaskId();
    }

    @Override
    default int runAsyncRepeating(@NotNull Runnable runnable, long period) {
        AtomicInteger taskId = new AtomicInteger();
        taskId.set(Bukkit.getScheduler().runTaskTimerAsynchronously((BukkitHuskHomes) getPlugin(),
                runnable, 0, period).getTaskId());
        return taskId.get();
    }

    @Override
    default void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

}
