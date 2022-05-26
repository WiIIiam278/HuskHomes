package net.william278.huskhomes;

import net.william278.huskhomes.cache.Cache;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.data.Database;
import net.william278.huskhomes.messenger.NetworkMessenger;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.teleport.TeleportManager;
import net.william278.huskhomes.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a cross-platform instance of the plugin
 */
public interface HuskHomes {

    /**
     * The platform plugin console logger
     *
     * @return the {@link Logger} implementation to use
     */
    Logger getLoggingAdapter();

    /**
     * The {@link Set} of online {@link Player}s on this server
     *
     * @return a {@link Set} of currently online {@link Player}s
     */
    List<Player> getOnlinePlayers();

    /**
     * The plugin {@link Settings} loaded from file
     *
     * @return the plugin {@link Settings}
     */
    Settings getSettings();

    /**
     * The plugin messages loaded from file
     *
     * @return The plugin {@link Locales}
     */
    Locales getLocales();

    /**
     * The {@link Database} that stores persistent plugin data
     *
     * @return the {@link Database} implementation for accessing data
     */
    Database getDatabase();

    /**
     * The {@link Cache} that holds cached data
     *
     * @return the plugin {@link Cache}
     */
    Cache getCache();

    /**
     * The {@link TeleportManager} that manages player teleports
     *
     * @return the {@link TeleportManager} implementation
     */
    TeleportManager getTeleportManager();

    /**
     * The {@link NetworkMessenger} that sends cross-network messages
     *
     * @return the {@link NetworkMessenger} implementation
     */
    @Nullable
    NetworkMessenger getNetworkMessenger();

    /**
     * Get the {@link Server} this server is on
     *
     * @return The server
     */
    CompletableFuture<Server> getServer(@NotNull Player requester);

    /**
     * Returns true if the position is a valid location on the server
     *
     * @param position The {@link Position} to check
     * @return {@code true} if the position is a valid one on the server; {@code false} otherwise
     */
    boolean isValidPositionOnServer(Position position);

    /**
     * (Re)-load config and message data from files
     */
    void loadConfigData();

    /**
     * Get the HuskHomes plugin version
     *
     * @return the HuskHomes plugin version string
     */
    String getPluginVersion();

    /**
     * Get the platform type (e.g. Spigot, Paper etc)
     *
     * @return the type of server platform string
     */
    String getPlatformType();

}
