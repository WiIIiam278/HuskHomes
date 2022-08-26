package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Warp;
import org.jetbrains.annotations.NotNull;

/**
 * Representation of an event that fires when a warp is created or updated
 */
public interface IWarpSaveEvent extends CancellableEvent {

    /**
     * Get the warp being created or updated
     *
     * @return the {@link Warp} being saved
     */
    @NotNull
    Warp getWarp();

}
