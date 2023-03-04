package net.william278.huskhomes.util;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface TaskRunner {
    int runAsync(@NotNull Runnable runnable);
    <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier);
    int runSync(@NotNull Runnable runnable);
    int runAsyncRepeating(@NotNull Runnable runnable, long delay);
    void cancelTask(int taskId);
    @NotNull
    HuskHomes getPlugin();

}
