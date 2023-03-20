package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an event that fires when a player requests to view a list of homes
 */
public interface IHomeListEvent extends Cancellable {

    /**
     * Get the list of homes to be displayed
     *
     * @return the list of homes
     */
    @NotNull
    List<Home> getHomes();

    void setHomes(@NotNull List<Home> homes);

    /**
     * Get the player viewing the home list
     *
     * @return the player viewing the home list
     */
    @NotNull
    CommandUser getListViewer();

    /**
     * Indicates if the player has requested to view a list of public homes
     *
     * @return true if the player has requested to view a list of public homes
     */
    boolean getIsPublicHomeList();

}
