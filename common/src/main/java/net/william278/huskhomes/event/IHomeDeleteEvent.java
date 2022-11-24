package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Home;
import org.jetbrains.annotations.NotNull;

/**
 * Representation of an event that fires when a single home is deleted
 */
public interface IHomeDeleteEvent extends CancellableEvent {

    /**
     * Get the home being deleted
     *
     * @return the {@link Home} being deleted
     */
    @NotNull
    Home getHome();

}
