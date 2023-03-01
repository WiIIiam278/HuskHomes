package net.william278.huskhomes.util;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface TaskRunner {
    void runAsync(@NotNull Runnable runnable);
    <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier);
    void runSync(@NotNull Runnable runnable);
    @NotNull
    HuskHomes getPlugin();

}
