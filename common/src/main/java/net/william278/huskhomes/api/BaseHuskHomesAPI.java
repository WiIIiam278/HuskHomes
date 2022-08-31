package net.william278.huskhomes.api;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.player.UserData;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.TeleportResult;
import net.william278.huskhomes.random.RandomTeleportEngine;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * The base implementation of the HuskHomes API, containing cross-platform API calls.
 * </p>
 * This class should not be used directly, but rather through platform-specific extending API classes.
 */
@SuppressWarnings("unused")
public abstract class BaseHuskHomesAPI {

    /**
     * <b>(Internal use only)</b> - Instance of the implementing plugin.
     */
    protected final HuskHomes plugin;

    /**
     * <b>(Internal use only)</b> - Constructor, instantiating the base API class
     */
    protected BaseHuskHomesAPI(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns saved {@link UserData} for the given player's account {@link UUID}, if they exist.
     *
     * @param uuid The {@link UUID} of the user to get data for.
     * @return The {@link UserData} of the user.
     * @since 3.0
     */
    public final CompletableFuture<Optional<UserData>> getUserData(@NotNull UUID uuid) {
        return plugin.getDatabase().getUserData(uuid);
    }

    /**
     * Returns saved {@link UserData} for the given player's username (case-insensitive), if they exist.
     *
     * @param username The username of the user to get data for.
     * @return The {@link UserData} of the user.
     * @since 3.0
     */
    public final CompletableFuture<Optional<UserData>> getUserData(@NotNull String username) {
        return plugin.getDatabase().getUserDataByName(username);
    }

    /**
     * Returns the last position, as used in the {@code /back} command, for this user
     *
     * @param user The {@link User} to get the last position for
     * @return The user's last {@link Position}, if there is one
     * @since 3.0
     */
    public CompletableFuture<Optional<Position>> getUserLastPosition(@NotNull User user) {
        return plugin.getDatabase().getLastPosition(user);
    }

    /**
     * Returns where the user last disconnected from a server
     *
     * @param user The {@link User} to get the last disconnect position for
     * @return The user's offline {@link Position}, if there is one
     * @since 3.0
     */
    public CompletableFuture<Optional<Position>> getUserOfflinePosition(@NotNull User user) {
        return plugin.getDatabase().getOfflinePosition(user);
    }

    /**
     * Returns where the user last set their spawn point by right-clicking a bed or respawn anchor
     * <p>
     * The optional returned by this method will be empty unless HuskHomes is running cross-server mode
     * and global respawning is enabled
     *
     * @param user The {@link User} to get the respawn position of
     * @return The user's respawn {@link Position} if they have one, otherwise an empty {@link Optional}
     * @since 3.0
     */
    public CompletableFuture<Optional<Position>> getUserRespawnPosition(@NotNull User user) {
        if (!plugin.getSettings().crossServer || plugin.getSettings().globalRespawning) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return plugin.getDatabase().getRespawnPosition(user);
    }

    /**
     * Save {@link UserData} to the database, updating it if data for the user already exists, or adding new user data if it doesn't.
     *
     * @param userData The {@link UserData} to save
     * @return A {@link CompletableFuture} that will complete when the data has been saved
     * @since 3.0
     */
    public final CompletableFuture<Void> saveUserData(@NotNull UserData userData) {
        return plugin.getDatabase().updateUserData(userData);
    }

    /**
     * Get a list of {@link Home}s set by the given user.
     *
     * @param user The {@link User} to get the homes of
     * @return A {@link CompletableFuture} that will complete with a list of {@link Home}s set by the user
     */
    public final CompletableFuture<List<Home>> getUserHomes(@NotNull User user) {
        return plugin.getDatabase().getHomes(user);
    }

    /**
     * Get a list of {@link Home}s set by the given user that they have made public
     *
     * @param user The {@link User} to get the public homes of
     * @return A {@link CompletableFuture} that will complete with a list of {@link Home}s set by the user that
     * they have made public (where {@link Home#isPublic})
     * @since 3.0
     */
    public final CompletableFuture<List<Home>> getUserPublicHomes(@NotNull User user) {
        return getUserHomes(user).thenApply(homes -> homes.stream()
                .filter(home -> home.isPublic)
                .collect(Collectors.toList()));
    }

    /**
     * Get a list of homes that have been made public ("public homes")
     *
     * @return A {@link CompletableFuture} that will complete with a list of public {@link Home}s
     * @since 3.0
     */
    public final CompletableFuture<List<Home>> getPublicHomes() {
        return plugin.getDatabase().getPublicHomes();
    }

    /**
     * Get a list of {@link Warp}s
     *
     * @return A {@link CompletableFuture} that will complete with a list of {@link Warp}s
     * @since 3.0
     */
    public final CompletableFuture<List<Warp>> getWarps() {
        return plugin.getDatabase().getWarps();
    }

    /**
     * Get the maximum number of set homes a given {@link OnlineUser} can make
     *
     * @param user The {@link OnlineUser} to get the maximum number of homes for
     * @return The maximum number of homes the user can set
     * @since 3.0
     */
    public final int getMaxHomeSlots(@NotNull OnlineUser user) {
        return user.getMaxHomes(plugin.getSettings().maxHomes, plugin.getSettings().stackPermissionLimits);
    }

    /**
     * Get the number of homes an {@link OnlineUser} can set for free
     * <p>
     * This is irrelevant unless the server is using economy features with HuskHomes
     *
     * @param user The {@link OnlineUser} to get the number of free home slots for
     * @return The number of homes the user can set for free
     * @since 3.0
     */
    public final int getFreeHomeSlots(@NotNull OnlineUser user) {
        return user.getFreeHomes(plugin.getSettings().freeHomeSlots, plugin.getSettings().stackPermissionLimits);
    }

    /**
     * Get the number of homes an {@link OnlineUser} can make public
     *
     * @param user The {@link OnlineUser} to get the number of public home slots for
     * @return The number of homes the user can make public
     * @since 3.0
     */
    public final int getMaxPublicHomeSlots(@NotNull OnlineUser user) {
        return user.getMaxPublicHomes(plugin.getSettings().maxPublicHomes, plugin.getSettings().stackPermissionLimits);
    }

    /**
     * Attempt to teleport an {@link OnlineUser} to a target {@link Position}
     *
     * @param user          The {@link OnlineUser} to teleport
     * @param position      The {@link Position} to teleport the user to
     * @param timedTeleport Whether the teleport should be timed or not (requiring a warmup where they must stand still
     *                      for a period of time)
     * @return A {@link CompletableFuture} that will complete with a {@link TeleportResult} indicating the result of
     * completing the teleport. If the teleport was successful, the {@link TeleportResult#successful} will be {@code true}.
     * @since 3.0
     */
    public final CompletableFuture<TeleportResult> teleportPlayer(@NotNull OnlineUser user, @NotNull Position position,
                                                                  boolean timedTeleport) {
        return timedTeleport ? plugin.getTeleportManager().timedTeleport(user, position)
                : plugin.getTeleportManager().teleport(user, position);
    }

    /**
     * Attempt to teleport an {@link OnlineUser} to a target {@link Position}
     *
     * @param user     The {@link OnlineUser} to teleport
     * @param position The {@link Position} to teleport the user to
     * @return A {@link CompletableFuture} that will complete with a {@link TeleportResult} indicating the result of
     * completing the teleport. If the teleport was successful, the {@link TeleportResult#successful} will be {@code true}.
     * @since 3.0
     */
    public final CompletableFuture<TeleportResult> teleportPlayer(@NotNull OnlineUser user, @NotNull Position position) {
        return teleportPlayer(user, position, false);
    }

    /**
     * Attempt to teleport an {@link OnlineUser} to a randomly generated {@link Position}. The {@link Position} will be
     * generated by the current {@link RandomTeleportEngine}
     *
     * @param user          The {@link OnlineUser} to teleport
     * @param timedTeleport Whether the teleport should be timed or not (requiring a warmup where they must stand still
     *                      for a period of time)
     * @param rtpArgs       Arguments that will be passed to the implementing {@link RandomTeleportEngine}
     * @return A {@link CompletableFuture} that will complete with a {@link TeleportResult} indicating the result of
     * completing the teleport. If the teleport was successful, the {@link TeleportResult#successful} will be {@code true}.
     * @since 3.0
     */
    public final CompletableFuture<TeleportResult> randomlyTeleportPlayer(@NotNull OnlineUser user, boolean timedTeleport,
                                                                          @NotNull String... rtpArgs) {
        return CompletableFuture.supplyAsync(() -> plugin.getRandomTeleportEngine().getRandomPosition(user.getPosition(), rtpArgs)
                .thenApply(position -> {
                    if (position.isPresent()) {
                        return teleportPlayer(user, position.get(), timedTeleport).join();
                    } else {
                        return TeleportResult.CANCELLED;
                    }
                }).join());
    }

    /**
     * Attempt to teleport an {@link OnlineUser} to a randomly generated {@link Position}. The {@link Position} will be
     * generated by the current {@link RandomTeleportEngine}
     *
     * @param user The {@link OnlineUser} to teleport
     * @return A {@link CompletableFuture} that will complete with a {@link TeleportResult} indicating the result of
     * completing the teleport. If the teleport was successful, the {@link TeleportResult#successful} will be {@code true}.
     * @since 3.0
     */
    public final CompletableFuture<TeleportResult> randomlyTeleportPlayer(@NotNull OnlineUser user) {
        return randomlyTeleportPlayer(user, false);
    }

    /**
     * Set the {@link RandomTeleportEngine} to use for processing random teleports. The engine will be used to process all
     * random teleports, including both {@code /rtp} command executions and API ({@link #randomlyTeleportPlayer(OnlineUser)})
     * calls.
     *
     * @param randomTeleportEngine the {@link RandomTeleportEngine} to use to process random teleports
     * @see RandomTeleportEngine
     * @since 3.0
     */
    public final void setRandomTeleportEngine(@NotNull RandomTeleportEngine randomTeleportEngine) {
        plugin.setRandomTeleportEngine(randomTeleportEngine);
    }

}
