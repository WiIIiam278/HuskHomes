package net.william278.huskhomes.event;

import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.position.Warp;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an event that fires when a player requests to view a list of warps
 */
public interface IWarpListEvent extends Cancellable {

    /**
     * Get the list of warps to be displayed
     *
     * @return the list of warps
     */
    List<Warp> getWarps();

    /**
     * Get the player viewing the warp list
     *
     * @return the player viewing the warp list
     */
    @NotNull
    OnlineUser getOnlineUser();

}
