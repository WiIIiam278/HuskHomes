package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Home;
import org.jetbrains.annotations.NotNull;

/**
 * Representation of an event that fires when a home is created or updated
 */
public interface IHomeSaveEvent extends CancellableEvent {

    /**
     * Get the home being created or updated
     *
     * @return the {@link Home} being saved
     */
    @NotNull
    Home getHome();

}
