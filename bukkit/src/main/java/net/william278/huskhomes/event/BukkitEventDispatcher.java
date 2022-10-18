package net.william278.huskhomes.event;

import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TimedTeleport;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BukkitEventDispatcher implements EventDispatcher {

    private final BukkitHuskHomes plugin;

    public BukkitEventDispatcher(@NotNull BukkitHuskHomes implementor) {
        this.plugin = implementor;
    }

    @Override
    public void dispatchTeleportEvent(@NotNull Teleport teleport) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            final TeleportEvent event = new TeleportEvent(teleport);
            Bukkit.getPluginManager().callEvent(event);
        });
    }

    @Override
    public CompletableFuture<ITeleportWarmupEvent> dispatchTeleportWarmupEvent(TimedTeleport teleport, int duration) {
        final CompletableFuture<ITeleportWarmupEvent> dispatchFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            final TeleportWarmupEvent event = new TeleportWarmupEvent(teleport, duration);
            Bukkit.getPluginManager().callEvent(event);
            dispatchFuture.complete(event);
        });
        return dispatchFuture;
    }

    @Override
    public CompletableFuture<IHomeSaveEvent> dispatchHomeSaveEvent(@NotNull Home home) {
        final CompletableFuture<IHomeSaveEvent> dispatchFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            final HomeSaveEvent event = new HomeSaveEvent(home);
            Bukkit.getPluginManager().callEvent(event);
            dispatchFuture.complete(event);
        });
        return dispatchFuture;
    }

    @Override
    public CompletableFuture<IHomeDeleteEvent> dispatchHomeDeleteEvent(@NotNull Home home) {
        final CompletableFuture<IHomeDeleteEvent> dispatchFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            final HomeDeleteEvent event = new HomeDeleteEvent(home);
            Bukkit.getPluginManager().callEvent(event);
            dispatchFuture.complete(event);
        });
        return dispatchFuture;
    }

    @Override
    public CompletableFuture<IWarpSaveEvent> dispatchWarpSaveEvent(@NotNull Warp warp) {
        final CompletableFuture<IWarpSaveEvent> dispatchFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            final WarpSaveEvent event = new WarpSaveEvent(warp);
            Bukkit.getPluginManager().callEvent(event);
            dispatchFuture.complete(event);
        });
        return dispatchFuture;
    }

    @Override
    public CompletableFuture<IWarpDeleteEvent> dispatchWarpDeleteEvent(@NotNull Warp warp) {
        final CompletableFuture<IWarpDeleteEvent> dispatchFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            final WarpDeleteEvent event = new WarpDeleteEvent(warp);
            Bukkit.getPluginManager().callEvent(event);
            dispatchFuture.complete(event);
        });
        return dispatchFuture;
    }

    @Override
    public CompletableFuture<IHomeListEvent> dispatchViewHomeListEvent(@NotNull List<Home> homes, @NotNull OnlineUser user,
                                                                       boolean publicHomeList) {
        final CompletableFuture<IHomeListEvent> dispatchFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            final HomeListEvent event = new HomeListEvent(homes, user, publicHomeList);
            Bukkit.getPluginManager().callEvent(event);
            dispatchFuture.complete(event);
        });
        return dispatchFuture;
    }

    @Override
    public CompletableFuture<IWarpListEvent> dispatchViewWarpListEvent(@NotNull List<Warp> warps, @NotNull OnlineUser user) {
        final CompletableFuture<IWarpListEvent> dispatchFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            final WarpListEvent event = new WarpListEvent(warps, user);
            Bukkit.getPluginManager().callEvent(event);
            dispatchFuture.complete(event);
        });
        return dispatchFuture;
    }

}
