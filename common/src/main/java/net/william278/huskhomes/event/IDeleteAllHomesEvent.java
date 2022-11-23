package net.william278.huskhomes.event;

import net.william278.huskhomes.player.User;
import org.jetbrains.annotations.NotNull;

/**
 * Representation of an event that fires when all homes are deleted
 */
public interface IDeleteAllHomesEvent extends CancellableEvent {

    /**
     * Get the player whose homes are being deleted
     *
     * @return the {@link User} whose homes are being deleted
     */
    @NotNull
    User getHomeOwner();

}
