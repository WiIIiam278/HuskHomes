package net.william278.huskhomes.event;

import net.william278.huskhomes.teleport.TimedTeleport;
import org.jetbrains.annotations.NotNull;

/**
 * Representation of an event that fires when a timed teleport warmup starts
 */
public interface ITeleportWarmupEvent extends CancellableEvent {

    /**
     * The duration of the timed teleport before the user is teleported
     *
     * @return the teleport warmup duration
     */
    int getWarmupDuration();

    /**
     * The {@link TimedTeleport} being processed
     *
     * @return the timed teleport that has started
     */
    @NotNull
    TimedTeleport getTimedTeleport();

}
