package net.william278.huskhomes.util;

import net.minecraft.server.ServerTask;
import net.william278.huskhomes.FabricHuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.function.Supplier;

public interface FabricTaskRunner extends TaskRunner {

    ConcurrentHashMap<Integer, CompletableFuture<?>> tasks = new ConcurrentHashMap<>();

    @Override
    default int runAsync(@NotNull Runnable runnable) {
        tasks.put(tasks.size(), CompletableFuture.runAsync(runnable, getPlugin().getMinecraftServer()));
        return tasks.size();
    }

    @Override
    default <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, getPlugin().getMinecraftServer());
    }

    @Override
    default void runSync(@NotNull Runnable runnable) {
        getPlugin().getMinecraftServer().executeSync(runnable);
    }

    @Override
    default int runAsyncRepeating(@NotNull Runnable runnable, long delay) {
        tasks.put(tasks.size(), CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    Thread.sleep(delay * 50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                runnable.run();
            }
        }, getPlugin().getMinecraftServer()));
        return tasks.size();
    }

    @Override
    default void runLater(@NotNull Runnable runnable, long delay) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delay * 50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            runnable.run();
        }, getPlugin().getMinecraftServer());
    }

    @Override
    default void cancelTask(int taskId) {
        tasks.get(taskId).cancel(true);
    }

    @Override
    default void cancelAllTasks() {
        tasks.forEach((taskId, future) -> future.cancel(true));
        tasks.clear();
    }

    @Override
    @NotNull
    FabricHuskHomes getPlugin();

}
