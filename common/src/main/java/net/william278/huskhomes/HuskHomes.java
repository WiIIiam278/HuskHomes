package net.william278.huskhomes;

import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.util.Logger;

import java.util.Set;

/**
 * Represents a cross-platform instance of the plugin
 */
public interface HuskHomes {

    /**
     * Returns the platform plugin console logger
     *
     * @return the {@link Logger} implementation to use
     */
    Logger getLogger();

    /**
     * Returns a {@link Set} of online {@link Player}s on this server
     *
     * @return a {@link Set} of currently online {@link Player}s
     */
    Set<Player> getOnlinePlayers();

}
