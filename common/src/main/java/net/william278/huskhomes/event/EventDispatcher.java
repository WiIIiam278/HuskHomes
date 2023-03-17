package net.william278.huskhomes.event;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TimedTeleport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * An abstract dispatcher of events
 */
public interface EventDispatcher {

    /**
     * Fire an event synchronously, then run a callback asynchronously
     *
     * @param event    The event to fire
     * @param callback The callback to run after the event has been fired
     * @param <T>      The type of event to fire
     */
    default <T extends Event> void fireEvent(@NotNull T event, @Nullable Consumer<T> callback) {
        getPlugin().runSync(() -> {
            if (!fireIsCancelled(event) && callback != null) {
                getPlugin().runAsync(() -> callback.accept(event));
            }
        });
    }

    /**
     * Fire an event on this thread, and return whether the event was cancelled
     *
     * @param event The event to fire
     * @param <T>   The type of event to fire
     * @return Whether the event was cancelled
     */
    <T extends Event> boolean fireIsCancelled(@NotNull T event);

    ITeleportEvent getTeleportEvent(@NotNull Teleport teleport);

    ITeleportWarmupEvent getTeleportWarmupEvent(@NotNull TimedTeleport teleport, int duration);

    IHomeSaveEvent getHomeSaveEvent(@NotNull Home home);

    IHomeDeleteEvent getHomeDeleteEvent(@NotNull Home home);

    IWarpSaveEvent getWarpSaveEvent(@NotNull Warp warp);

    IWarpDeleteEvent getWarpDeleteEvent(@NotNull Warp warp);

    IHomeListEvent getViewHomeListEvent(@NotNull List<Home> homes, @NotNull OnlineUser user, boolean publicHomeList);

    IWarpListEvent getViewWarpListEvent(@NotNull List<Warp> homes, @NotNull OnlineUser user);

    IDeleteAllHomesEvent getDeleteAllHomesEvent(@NotNull User user);

    IDeleteAllWarpsEvent getDeleteAllWarpsEvent();

    @NotNull
    HuskHomes getPlugin();

}
