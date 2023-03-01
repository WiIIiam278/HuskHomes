package net.william278.huskhomes.util;

import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.HuskHomes;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface BukkitTaskRunner extends TaskRunner {
    @Override
    default void runAsync(@NotNull Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously((BukkitHuskHomes) getPlugin(), runnable);
    }

    @Override
    default <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously((BukkitHuskHomes) getPlugin(),
                () -> future.complete(supplier.get()));
        return future;
    }

    @Override
    default void runSync(@NotNull Runnable runnable) {
        Bukkit.getScheduler().runTask((BukkitHuskHomes) getPlugin(), runnable);
    }

}
