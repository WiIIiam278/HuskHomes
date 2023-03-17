package net.william278.huskhomes.event;

import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TimedTeleport;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BukkitEventDispatcher extends EventDispatcher {


    @Override
    default <T extends Event> boolean fireIsCancelled(@NotNull T event) {
        Bukkit.getPluginManager().callEvent((org.bukkit.event.Event) event);
        return event instanceof Cancellable cancellable && cancellable.isCancelled();
    }

    @Override
    default ITeleportEvent getTeleportEvent(@NotNull Teleport teleport) {
        return new TeleportEvent(teleport);
    }

    @Override
    default ITeleportWarmupEvent getTeleportWarmupEvent(@NotNull TimedTeleport teleport, int duration) {
        return new TeleportWarmupEvent(teleport, duration);
    }


    @Override
    default IHomeSaveEvent getHomeSaveEvent(@NotNull Home home) {
        return new HomeSaveEvent(home);
    }

    @Override
    default IHomeDeleteEvent getHomeDeleteEvent(@NotNull Home home) {
        return new HomeDeleteEvent(home);
    }

    @Override
    default IWarpSaveEvent getWarpSaveEvent(@NotNull Warp warp) {
        return new WarpSaveEvent(warp);
    }

    @Override
    default IWarpDeleteEvent getWarpDeleteEvent(@NotNull Warp warp) {
        return new WarpDeleteEvent(warp);
    }

    @Override
    default IHomeListEvent getViewHomeListEvent(@NotNull List<Home> homes, @NotNull CommandUser listViewer, boolean publicHomeList) {
        return new HomeListEvent(homes, listViewer, publicHomeList);
    }

    @Override
    default IWarpListEvent getViewWarpListEvent(@NotNull List<Warp> warps, @NotNull CommandUser listViewer) {
        return new WarpListEvent(warps, listViewer);
    }

    @Override
    default IDeleteAllHomesEvent getDeleteAllHomesEvent(@NotNull User user) {
        return new DeleteAllHomesEvent(user);
    }

    @Override
    default IDeleteAllWarpsEvent getDeleteAllWarpsEvent() {
        return new DeleteAllWarpsEvent();
    }

}
