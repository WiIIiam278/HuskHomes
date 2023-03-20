package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

/**
 * Representation of an event that fires when a warp is created or updated
 */
public interface IWarpEditEvent extends Cancellable {

    /**
     * Get the warp being created or updated
     *
     * @return the {@link Warp} being saved
     */
    @NotNull
    Warp getWarp();

    @NotNull
    CommandUser getEditor();

}
