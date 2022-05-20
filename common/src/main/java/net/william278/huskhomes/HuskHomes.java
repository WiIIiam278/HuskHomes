package net.william278.huskhomes;

import net.william278.huskhomes.config.Messages;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.position.Server;
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
    Logger getLoggingAdapter();

    /**
     * Returns a {@link Set} of online {@link Player}s on this server
     *
     * @return a {@link Set} of currently online {@link Player}s
     */
    Set<Player> getOnlinePlayers();

    /**
     * Return the plugin {@link Settings} loaded from file
     *
     * @return the plugin {@link Settings}
     */
    Settings getSettings();

    /**
     * Return the plugin messages loaded from file
     *
     * @return The plugin {@link Messages}
     */
    Messages getMessages();

    /**
     * Return the {@link Server} the plugin is running on
     *
     * @return the {@link Server} object representing the implementing server
     */
    Server getServerData();

}
