package net.william278.huskhomes.event;

import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Home;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an event that fires when a player requests to view a list of homes
 */
public interface IHomeListEvent extends CancellableEvent {

    /**
     * Get the list of homes to be displayed
     *
     * @return the list of homes
     */
    List<Home> getHomes();

    /**
     * Get the player viewing the home list
     *
     * @return the player viewing the home list
     */
    @NotNull
    OnlineUser getOnlineUser();

    /**
     * Indicates if the player has requested to view a list of public homes
     *
     * @return true if the player has requested to view a list of public homes
     */
    boolean getIsPublicHomeList();

}
