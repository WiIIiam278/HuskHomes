package net.william278.huskhomes.event;

import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TimedTeleport;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * An abstract dispatcher of events
 */
public interface EventDispatcher {

    void dispatchTeleportEvent(@NotNull Teleport teleport);

    CompletableFuture<ITeleportWarmupEvent> dispatchTeleportWarmupEvent(TimedTeleport teleport, int duration);

    CompletableFuture<IHomeSaveEvent> dispatchHomeSaveEvent(@NotNull Home home);

    CompletableFuture<IHomeDeleteEvent> dispatchHomeDeleteEvent(@NotNull Home home);

    CompletableFuture<IWarpSaveEvent> dispatchWarpSaveEvent(@NotNull Warp warp);

    CompletableFuture<IWarpDeleteEvent> dispatchWarpDeleteEvent(@NotNull Warp warp);

    CompletableFuture<IHomeListEvent> dispatchViewHomeListEvent(@NotNull List<Home> homes, @NotNull OnlineUser user,
                                                                boolean publicHomeList);

    CompletableFuture<IWarpListEvent> dispatchViewWarpListEvent(@NotNull List<Warp> homes, @NotNull OnlineUser user);

}
