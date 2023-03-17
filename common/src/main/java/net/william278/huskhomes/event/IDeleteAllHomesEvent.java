package net.william278.huskhomes.event;

import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * Representation of an event that fires when all homes are deleted
 */
public interface IDeleteAllHomesEvent extends Cancellable {

    /**
     * Get the player whose homes are being deleted
     *
     * @return the {@link User} whose homes are being deleted
     */
    @NotNull
    User getHomeOwner();

}
