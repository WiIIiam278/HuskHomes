package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * An abstract dispatcher of events
 */
public interface EventDispatcher {

    CompletableFuture<ITeleportEvent> dispatchTeleportEvent(@NotNull Teleport teleport);

    CompletableFuture<IHomeSaveEvent> dispatchHomeSaveEvent(@NotNull Home home);

    CompletableFuture<IHomeDeleteEvent> dispatchHomeDeleteEvent(@NotNull Home home);

    CompletableFuture<IWarpSaveEvent> dispatchWarpSaveEvent(@NotNull Warp warp);

    CompletableFuture<IWarpDeleteEvent> dispatchWarpDeleteEvent(@NotNull Warp warp);

}
