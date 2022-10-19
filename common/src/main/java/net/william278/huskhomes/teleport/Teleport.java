package net.william278.huskhomes.teleport;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.messenger.Message;
import net.william278.huskhomes.messenger.MessagePayload;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Represents a teleport in the process of being executed
 *
 * @since 3.1
 */
public class Teleport {

    /**
     * <b>Internal</b> - Instance of the implementing HuskHomes plugin
     */
    @NotNull
    private final HuskHomes plugin;

    /**
     * The {@link User} who is doing the teleporting
     */
    @Nullable
    public final User teleporter;

    /**
     * The {@link OnlineUser} on this server executing the teleport.
     * </p>
     * Can be the same as the {@link #teleporter}, but not necessarily
     */
    @NotNull
    public final OnlineUser executor;

    /**
     * The target {@link Position} the teleporter should be teleported to
     */
    @Nullable
    public final Position target;

    /**
     * The {@link TeleportType type} of the teleport
     */
    @NotNull
    public final TeleportType type;

    /**
     * {@link Settings.EconomyAction}s to be checked against the {@link #executor executor}'s balance
     */
    @NotNull
    public final Set<Settings.EconomyAction> economyActions;

    /**
     * <b>Internal</b> - use TeleportBuilder to instantiate a teleport
     */
    protected Teleport(@Nullable User teleporter, @NotNull OnlineUser executor, @Nullable Position target,
                       @NotNull TeleportType type, @NotNull Set<Settings.EconomyAction> economyActions,
                       @NotNull HuskHomes plugin) {
        this.teleporter = teleporter;
        this.executor = executor;
        this.target = target;
        this.type = type;
        this.economyActions = economyActions;
        this.plugin = plugin;
    }

    /**
     * Create a teleport builder with an executor to carry it out
     *
     * @param plugin   Instance of the implementing HuskHomes plugin
     * @param executor The {@link OnlineUser} on this server executing the teleport.
     *                 Not necessarily the person being teleported, which can be set independently with
     *                 {{@link TeleportBuilder#setTeleporter(User)}}
     * @return A new {@link TeleportBuilder} instance
     */
    public static TeleportBuilder builder(@NotNull HuskHomes plugin, @NotNull OnlineUser executor) {
        return new TeleportBuilder(plugin, executor);
    }

    /**
     * Execute this teleport
     *
     * @return A {@link CompletableFuture} that completes when the teleport is finished
     */
    public CompletableFuture<TeleportResult> execute() {
        // Validate the teleporter
        if (teleporter == null) {
            return CompletableFuture.completedFuture(TeleportResult.ResultState.FAILED_TELEPORTER_NOT_RESOLVED)
                    .thenApply(resultState -> TeleportResult.from(resultState, this));
        }

        // Validate the target
        if (target == null) {
            return CompletableFuture.completedFuture(TeleportResult.ResultState.FAILED_TARGET_NOT_RESOLVED)
                    .thenApply(resultState -> TeleportResult.from(resultState, this));
        }

        // Check economy actions
        for (Settings.EconomyAction economyAction : economyActions) {
            if (!plugin.validateEconomyCheck(executor, economyAction)) {
                return CompletableFuture.completedFuture(TeleportResult.ResultState.CANCELLED_ECONOMY)
                        .thenApply(resultState -> TeleportResult.from(resultState, this));
            }
        }

        // Run the teleport and apply economy actions
        return run(teleporter)
                .thenApply(resultState -> TeleportResult.from(resultState, this))
                .thenApply(result -> {
                    if (teleporter instanceof OnlineUser user) {
                        finish(user, result);
                    }
                    return result;
                });
    }

    /**
     * Run the teleport, either locally or over the network
     *
     * @param teleporter The {@link User} who is doing the teleporting
     * @return A {@link CompletableFuture} that completes when the teleport is finished
     */
    private CompletableFuture<TeleportResult.ResultState> run(@NotNull User teleporter) {
        return teleporter instanceof OnlineUser onlineUser
                ? teleportLocalUser(onlineUser)
                : teleportNetworkedUser();
    }

    /**
     * Finish the teleport, finalize economy transactions if successful and send message
     *
     * @param teleporter The {@link OnlineUser} who was teleported
     * @param result     The {@link TeleportResult.ResultState} of the teleport
     */
    private void finish(@NotNull OnlineUser teleporter, @NotNull TeleportResult result) {
        if (result.successful()) {
            // Complete economy transactions
            economyActions.forEach(economyAction -> plugin.performEconomyTransaction(executor, economyAction));

            // Play sound effect if successful
            plugin.getSettings().getSoundEffect(Settings.SoundEffectAction.TELEPORTATION_COMPLETE)
                    .ifPresent(teleporter::playSound);
        }

        // Send result message
        result.sendResultMessage(plugin.getLocales(), teleporter);
    }

    /**
     * Teleport a local user
     *
     * @param teleporter The teleporter
     * @return A {@link CompletableFuture} that completes when the teleport is finished
     */
    private CompletableFuture<TeleportResult.ResultState> teleportLocalUser(@NotNull OnlineUser teleporter) {
        assert target != null;

        // Dispatch the teleport event and update the player's last position
        plugin.getEventDispatcher().dispatchTeleportEvent(this);
        if (!plugin.getSettings().backCommandSaveOnTeleportEvent && type == TeleportType.TELEPORT) {
            plugin.getDatabase().setLastPosition(teleporter, teleporter.getPosition());
        }

        // If the target position is local, finalize economy transactions and teleport the player
        if (!plugin.getSettings().crossServer || target.server.equals(plugin.getPluginServer())) {
            return teleporter.teleportLocally(target, plugin.getSettings().asynchronousTeleports);
        }

        // If the target position is on another server, execute a cross-server teleport
        final CompletableFuture<TeleportResult.ResultState> teleportFuture = new CompletableFuture<>();
        plugin.getDatabase()
                .setCurrentTeleport(teleporter, this)
                .thenApply(ignored -> plugin
                        .getNetworkMessenger()
                        .sendPlayer(teleporter, target.server)
                        .thenApply(completed -> completed
                                ? TeleportResult.ResultState.COMPLETED_CROSS_SERVER
                                : TeleportResult.ResultState.FAILED_INVALID_SERVER))
                .orTimeout(10, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    plugin.getLoggingAdapter().log(Level.WARNING, "Cross-server teleport timed out for " + teleporter.username);
                    plugin.getDatabase().setCurrentTeleport(teleporter, null);
                    return CompletableFuture.completedFuture(TeleportResult.ResultState.FAILED_INVALID_SERVER);
                })
                .thenAccept(result -> result.thenAcceptAsync(teleportFuture::complete));
        return teleportFuture;
    }

    /**
     * Teleport a user not on this server but on the proxy network
     *
     * @return A {@link CompletableFuture} that completes when the teleport is finished.
     * Successful cross-server teleports will return {@link TeleportResult.ResultState#COMPLETED_CROSS_SERVER}.
     * @implNote Cross-server teleports will return with a {@link TeleportResult.ResultState#FAILED_INVALID_SERVER} result if the
     * target server is not online, or connection timed out
     */
    private CompletableFuture<TeleportResult.ResultState> teleportNetworkedUser() {
        assert target != null;
        assert teleporter != null;

        // Send a network message to a user on another server to teleport them to the target position
        return plugin.getNetworkMessenger()
                .sendMessage(executor,
                        new Message(Message.MessageType.TELEPORT_TO_POSITION_REQUEST,
                                executor.username,
                                teleporter.username,
                                MessagePayload.withPosition(target),
                                Message.RelayType.MESSAGE,
                                plugin.getSettings().clusterId))
                .orTimeout(3, TimeUnit.SECONDS)
                .exceptionally(throwable -> null)
                .thenApply(result -> {
                    if (result == null || result.payload.resultState == null) {
                        return TeleportResult.ResultState.FAILED_TELEPORTER_NOT_RESOLVED;
                    }
                    return result.payload.resultState;
                });
    }

}
