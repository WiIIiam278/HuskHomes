package net.william278.huskhomes;

import net.william278.huskhomes.cache.Cache;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.messenger.NetworkMessenger;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.SavedPositionManager;
import net.william278.huskhomes.teleport.TeleportManager;
import net.william278.huskhomes.util.Logger;
import net.william278.huskhomes.util.Version;
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
    @NotNull
    Logger getLoggingAdapter();

    /**
     * The {@link Set} of online {@link OnlineUser}s on this server
     *
     * @return a {@link Set} of currently online {@link OnlineUser}s
     */
    @NotNull
    List<OnlineUser> getOnlinePlayers();

    /**
     * The plugin {@link Settings} loaded from file
     *
     * @return the plugin {@link Settings}
     */
    @NotNull
    Settings getSettings();

    /**
     * The plugin messages loaded from file
     *
     * @return The plugin {@link Locales}
     */
    @NotNull
    Locales getLocales();

    /**
     * The {@link Database} that stores persistent plugin data
     *
     * @return the {@link Database} implementation for accessing data
     */
    @NotNull
    Database getDatabase();

    /**
     * The {@link Cache} that holds cached data
     *
     * @return the plugin {@link Cache}
     */
    @NotNull
    Cache getCache();

    /**
     * The {@link TeleportManager} that manages player teleports
     *
     * @return the {@link TeleportManager} implementation
     */
    @NotNull
    TeleportManager getTeleportManager();

    /**
     * The {@link SavedPositionManager} that manages setting homes and warps
     *
     * @return the {@link SavedPositionManager} implementation
     */
    @NotNull
    SavedPositionManager getSavedPositionManager();

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
    CompletableFuture<Server> getServer(@NotNull OnlineUser requester);

    /**
     * Returns true if the position is a valid location on the server
     *
     * @param position The {@link Position} to check
     * @return {@code true} if the position is a valid one on the server; {@code false} otherwise
     */
    boolean isValidPositionOnServer(Position position);

    /**
     * Returns the plugin version
     *
     * @return the plugin {@link Version}
     */
    @NotNull
    Version getPluginVersion();

    /**
     * Returns the Minecraft version implementation
     *
     * @return the Minecraft {@link Version}
     */
    @NotNull
    Version getMinecraftVersion();

    /**
     * Get the platform type (e.g. Spigot, Paper etc)
     *
     * @return the type of server platform string
     */
    @NotNull
    String getPlatformType();

    /**
     * Reloads the {@link Settings} and {@link Locales} from their respective config files
     *
     * @return a {@link CompletableFuture} that will be completed when the plugin reload is complete and if it was successful
     */
    CompletableFuture<Boolean> reload();
}
