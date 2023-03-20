package net.william278.huskhomes.event;

import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

/**
 * Representation of an event that fires when all warps are deleted
 */
public interface IDeleteAllWarpsEvent extends Cancellable {

    @NotNull
    CommandUser getDeleter();

}
