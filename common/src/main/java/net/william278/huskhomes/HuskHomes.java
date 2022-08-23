package net.william278.huskhomes;

import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.config.Spawn;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.hook.PluginHook;
import net.william278.huskhomes.messenger.NetworkMessenger;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.random.RtpEngine;
import net.william278.huskhomes.request.RequestManager;
import net.william278.huskhomes.teleport.TeleportManager;
import net.william278.huskhomes.util.Logger;
import net.william278.huskhomes.util.Permission;
import net.william278.huskhomes.util.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
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
     * Finds a {@link OnlineUser} by their name. Auto-completes partially typed names for the closest match
     *
     * @param playerName the name of the player to find
     * @return an {@link Optional} containing the {@link OnlineUser} if found, or an empty {@link Optional} if not found
     */
    default Optional<OnlineUser> findPlayer(@NotNull String playerName) {
        return Optional.ofNullable(getOnlinePlayers().stream()
                .filter(user -> user.username.equalsIgnoreCase(playerName))
                .findFirst()
                .orElse(getOnlinePlayers().stream()
                        .filter(user -> user.username.toLowerCase().startsWith(playerName.toLowerCase()))
                        .findFirst().orElse(null)));
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
     * The {@link TeleportManager} that manages player teleports
     *
     * @return the plugin {@link TeleportManager}
     */
    @NotNull
    TeleportManager getTeleportManager();

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
    @Nullable
    NetworkMessenger getNetworkMessenger();

    /**
     * The {@link RtpEngine} that manages random teleports
     *
     * @return the {@link RtpEngine} implementation
     */
    @NotNull
    RtpEngine getRtpEngine();

    /**
     * The {@link Spawn} location of this server
     *
     * @return the {@link Spawn} location data
     */
    Optional<Spawn> getServerSpawn();

    /**
     * Update the {@link Spawn} position to a location on the server
     *
     * @param location the new {@link Spawn} location
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
    CompletableFuture<Optional<Location>> getSafeGroundLocation(@NotNull Location location);

    /**
     * Get the {@link Server} this server is on
     *
     * @return The server
     */
    @NotNull
    Server getServer(@NotNull OnlineUser requester);

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
