package net.william278.huskhomes.event;

import net.william278.huskhomes.teleport.Teleport;
import org.jetbrains.annotations.NotNull;

/**
 * Representation of an event that fires when a teleport is about to occur
 */
public interface ITeleportEvent {

    /**
     * Get the teleport being carried out
     *
     * @return the {@link Teleport} being carried out
     */
    @NotNull
    Teleport getTeleport();

}
