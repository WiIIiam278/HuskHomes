package net.william278.huskhomes;

import net.william278.desertwell.UpdateChecker;
import net.william278.desertwell.Version;
import net.william278.huskhomes.command.CommandBase;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.config.CachedSpawn;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.event.EventDispatcher;
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.hook.MapHook;
import net.william278.huskhomes.hook.PluginHook;
import net.william278.huskhomes.messenger.NetworkMessenger;
import net.william278.huskhomes.migrator.Migrator;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.request.RequestManager;
import net.william278.huskhomes.util.Logger;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a cross-platform instance of the plugin
 */
public interface HuskHomes {

    int SPIGOT_RESOURCE_ID = 83767;

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
     * Finds a local {@link OnlineUser} by their name. Auto-completes partially typed names for the closest match
     *
     * @param playerName the name of the player to find
     * @return an {@link Optional} containing the {@link OnlineUser} if found, or an empty {@link Optional} if not found
     */
    @NotNull
    default Optional<OnlineUser> findOnlinePlayer(@NotNull String playerName) {
        return getOnlinePlayers().stream()
                .filter(user -> user.username.equalsIgnoreCase(playerName))
                .findFirst()
                .or(() -> getOnlinePlayers().stream()
                        .filter(user -> user.username.toLowerCase().startsWith(playerName.toLowerCase()))
                        .findFirst());
    }

    /**
     * Looks for a player logged in either on this server or the network of connected servers, by approximate name.
     * Auto-completes partially typed names for the closest match.
     *
     * @param requester        the player requesting the player to be found
     * @param targetPlayerName the name of the player to find
     * @return an {@link Optional} containing the {@link OnlineUser} if found, or an empty {@link Optional} if not found
     */
    @NotNull
    default CompletableFuture<Optional<String>> findPlayer(@NotNull OnlineUser requester, @NotNull String targetPlayerName) {
        if (requester.username.equalsIgnoreCase(targetPlayerName)) {
            return CompletableFuture.completedFuture(Optional.of(requester.username));
        }
        final Optional<OnlineUser> localPlayer = findOnlinePlayer(targetPlayerName);
        if (localPlayer.isPresent()) {
            return CompletableFuture.completedFuture(Optional.of(localPlayer.get().username));
        }
        if (getSettings().crossServer) {
            return getNetworkMessenger().findPlayer(requester, targetPlayerName);
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

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
     * The {@link RequestManager} that manages player requests
     *
     * @return the plugin {@link RequestManager}
     */
    @NotNull RequestManager getRequestManager();

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
    @NotNull
    NetworkMessenger getNetworkMessenger() throws HuskHomesException;

    /**
     * The {@link RandomTeleportEngine} that manages random teleports
     *
     * @return the {@link RandomTeleportEngine} implementation
     */
    @NotNull
    RandomTeleportEngine getRandomTeleportEngine();

    /**
     * Sets the {@link RandomTeleportEngine} to be used for processing random teleports
     *
     * @param randomTeleportEngine the {@link RandomTeleportEngine} to use
     */
    void setRandomTeleportEngine(@NotNull RandomTeleportEngine randomTeleportEngine);

    /**
     * The {@link EventDispatcher} that dispatches API events
     *
     * @return the {@link EventDispatcher} implementation
     */
    @NotNull
    EventDispatcher getEventDispatcher();

    /**
     * The list of available {@link Migrator}s
     *
     * @return the list of available {@link Migrator}
     */
    List<Migrator> getMigrators();

    /**
     * The local {@link CachedSpawn} location of this server, as cached to disk
     *
     * @return the {@link CachedSpawn} location data
     * @see #getSpawn() for the canonical spawn point to use
     */
    Optional<CachedSpawn> getLocalCachedSpawn();

    /**
     * The canonical spawn {@link Position} of this server, if it has been set
     *
     * @return the {@link Position} of the spawn, or an empty {@link Optional} if it has not been set
     */
    default CompletableFuture<Optional<? extends Position>> getSpawn() {
        return CompletableFuture.supplyAsync(() -> getSettings().crossServer && getSettings().globalSpawn
                ? getDatabase().getWarp(getSettings().globalSpawnName).join()
                : getLocalCachedSpawn().flatMap(spawn -> spawn.getPosition(getPluginServer())));
    }

    /**
     * Returns a future returning the latest plugin {@link Version} if the plugin is out-of-date
     *
     * @return a {@link CompletableFuture} returning the latest {@link Version} if the current one is out-of-date
     */
    default CompletableFuture<Optional<Version>> getLatestVersionIfOutdated() {
        final UpdateChecker updateChecker = UpdateChecker.create(getPluginVersion(), SPIGOT_RESOURCE_ID);
        return updateChecker.isUpToDate().thenApply(upToDate -> {
            if (upToDate) {
                return Optional.empty();
            } else {
                return Optional.of(updateChecker.getLatestVersion().join());
            }
        });
    }

    /**
     * Update the {@link CachedSpawn} position to a location on the server
     *
     * @param location the new {@link CachedSpawn} location
     */
    void setServerSpawn(@NotNull Location location);

    /**
     * Set of active {@link PluginHook}s running on the server
     *
     * @return the {@link Set} of active {@link PluginHook}s
     */
    @NotNull
    Set<PluginHook> getPluginHooks();

    /**
     * Gets the {@link MapHook} being used to display public homes and warps on a web map, if there is one, and it is enabled
     *
     * @return the {@link MapHook} being used, if there is one, and it is enabled
     */
    default Optional<MapHook> getMapHook() {
        return getSettings().doMapHook ? getPluginHooks()
                .stream()
                .filter(pluginHook -> pluginHook instanceof MapHook)
                .findFirst()
                .map(hook -> (MapHook) hook) : Optional.empty();
    }

    /**
     * Perform an economy check on the {@link OnlineUser}; returning {@code true} if it passes the check
     *
     * @param player the player to perform the check on
     * @param action the action to perform
     * @return {@code true} if the action passes the check, {@code false} if the user has insufficient funds
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean validateEconomyCheck(@NotNull OnlineUser player, @NotNull Settings.EconomyAction action) {
        final Optional<Double> cost = getSettings().getEconomyCost(action).map(Math::abs);
        if (cost.isPresent() && !player.hasPermission(Permission.BYPASS_ECONOMY_CHECKS.node)) {
            final Optional<EconomyHook> hook = getPluginHooks().stream().filter(pluginHook ->
                    pluginHook instanceof EconomyHook).findFirst().map(pluginHook -> (EconomyHook) pluginHook);
            if (hook.isPresent()) {
                if (cost.get() > hook.get().getPlayerBalance(player)) {
                    getLocales().getLocale("error_insufficient_funds", hook.get().formatCurrency(cost.get()))
                            .ifPresent(player::sendMessage);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Execute an economy transaction if needed, updating the player's balance
     *
     * @param player the player to deduct the cost from if needed
     * @param action the action to deduct the cost from if needed
     */
    default void performEconomyTransaction(@NotNull OnlineUser player, @NotNull Settings.EconomyAction action) {
        if (!getSettings().economy) return;
        final Optional<Double> cost = getSettings().getEconomyCost(action).map(Math::abs);

        if (cost.isPresent() && !player.hasPermission(Permission.BYPASS_ECONOMY_CHECKS.node)) {
            final Optional<EconomyHook> hook = getPluginHooks().stream().filter(pluginHook ->
                    pluginHook instanceof EconomyHook).findFirst().map(pluginHook -> (EconomyHook) pluginHook);
            if (hook.isPresent()) {
                hook.get().changePlayerBalance(player, -cost.get());
                getLocales().getLocale(action.confirmationLocaleId, hook.get().formatCurrency(cost.get()))
                        .ifPresent(player::sendMessage);
            }
        }
    }

    /**
     * Returns a safe ground location for the specified {@link Location} if possible
     *
     * @param location the {@link Location} to find a safe ground location for
     * @return a {@link CompletableFuture} that will complete with an optional of the safe ground position, if it is
     * possible to find one
     */
    CompletableFuture<Optional<Location>> resolveSafeGroundLocation(@NotNull Location location);

    /**
     * Returns the {@link Server} the plugin is on
     *
     * @return The {@link Server} object
     * @throws HuskHomesException If the server has not been initialized
     */
    @NotNull
    Server getPluginServer() throws HuskHomesException;

    /**
     * Fetches the name of this server if {@link Server} is {@code null} by querying the proxy
     *
     * @param requester The {@link OnlineUser} to carry out the proxy request
     * @return a future completing when the server has been fetched
     * @implNote If cross-server mode is disabled, or the server has already been pulled from the server.yml cache file,
     * the future will return immediately
     */
    CompletableFuture<Void> fetchServer(@NotNull OnlineUser requester);

    /**
     * Returns a resource read from the plugin resources folder
     *
     * @param name the name of the resource
     * @return the resource read as an {@link InputStream}
     */
    @Nullable
    InputStream getResource(@NotNull String name);

    /**
     * Returns the plugin data folder containing the plugin config, etc
     *
     * @return the plugin data folder
     */
    @NotNull
    File getDataFolder();

    /**
     * Returns a list of worlds on the server
     *
     * @return a list of worlds on the server
     */
    @NotNull
    List<World> getWorlds();

    /**
     * Returns the plugin version
     *
     * @return the plugin {@link Version}
     */
    @NotNull
    Version getPluginVersion();

    /**
     * Returns a list of enabled commands
     *
     * @return A list of registered and enabled {@link CommandBase}s
     */
    @NotNull
    List<CommandBase> getCommands();

    /**
     * Reloads the {@link Settings} and {@link Locales} from their respective config files
     *
     * @return a {@link CompletableFuture} that will be completed when the plugin reload is complete and if it was successful
     */
    CompletableFuture<Boolean> reload();

    /**
     * Registers the plugin with bStats metrics
     *
     * @param metricsId the bStats id for the plugin
     */
    void registerMetrics(int metricsId);

}
