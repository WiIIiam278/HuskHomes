package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

/**
 * Representation of an event that fires when a home is edited
 */
public interface IHomeEditEvent extends Cancellable {

    /**
     * Get the home about to be saved
     *
     * @return the {@link Home} being saved
     */
    @NotNull
    Home getHome();

    @NotNull
    CommandUser getEditor();

}
