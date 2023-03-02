package net.william278.huskhomes.teleport;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
     * {@link EconomyHook.EconomyAction}s to be checked against the {@link #executor executor}'s balance
     */
    @NotNull
    public final Set<EconomyHook.EconomyAction> economyActions;

    /**
     * Whether to update the {@link #teleporter teleporter}'s last position (i.e. their {@code /back} position)
     */
    public final boolean updateLastPosition;

    /**
     * <b>Internal</b> - use TeleportBuilder to instantiate a teleport
     */
    protected Teleport(@Nullable User teleporter, @NotNull OnlineUser executor, @Nullable Position target,
                       @NotNull TeleportType type, @NotNull Set<EconomyHook.EconomyAction> economyActions,
                       final boolean updateLastPosition, @NotNull HuskHomes plugin) {
        this.teleporter = teleporter;
        this.executor = executor;
        this.target = target;
        this.type = type;
        this.economyActions = economyActions;
        this.plugin = plugin;
        this.updateLastPosition = updateLastPosition;
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
    public CompletableFuture<CompletedTeleport> execute() {
        // Validate the teleporter
        if (teleporter == null) {
            return CompletableFuture.completedFuture(TeleportResult.FAILED_TELEPORTER_NOT_RESOLVED)
                    .thenApply(resultState -> CompletedTeleport.from(resultState, this));
        }

        // Validate the target
        if (target == null) {
            return CompletableFuture.completedFuture(TeleportResult.FAILED_TARGET_NOT_RESOLVED)
                    .thenApply(resultState -> CompletedTeleport.from(resultState, this));
        }

        // Check economy actions
        for (EconomyHook.EconomyAction economyAction : economyActions) {
            if (!plugin.validateEconomyCheck(executor, economyAction)) {
                return CompletableFuture.completedFuture(TeleportResult.CANCELLED_ECONOMY)
                        .thenApply(resultState -> CompletedTeleport.from(resultState, this));
            }
        }

        // Run the teleport and apply economy actions
        return run(teleporter)
                .thenApply(resultState -> CompletedTeleport.from(resultState, this))
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
    private CompletableFuture<TeleportResult> run(@NotNull User teleporter) {
        return teleporter instanceof OnlineUser onlineUser
                ? teleportLocalUser(onlineUser)
                : teleportNetworkedUser();
    }

    /**
     * Finish the teleport, finalize economy transactions if successful and send message
     *
     * @param teleporter The {@link OnlineUser} who was teleported
     * @param result     The {@link TeleportResult} of the teleport
     */
    private void finish(@NotNull OnlineUser teleporter, @NotNull CompletedTeleport result) {
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
    private CompletableFuture<TeleportResult> teleportLocalUser(@NotNull OnlineUser teleporter) {
        assert target != null;

        // Dispatch the teleport event and update the player's last position
        plugin.getEventDispatcher().dispatchTeleportEvent(this);
        if (updateLastPosition && !plugin.getSettings().isBackCommandSaveOnTeleportEvent() && type == TeleportType.TELEPORT) {
            plugin.getDatabase().setLastPosition(teleporter, teleporter.getPosition());
        }

        // If the target position is local, finalize economy transactions and teleport the player
        if (!plugin.getSettings().isCrossServer() || target.getServer().equals(plugin.getServerName())) {
            return teleporter.teleportLocally(target, plugin.getSettings().isAsynchronousTeleports());
        }

        // If the target position is on another server, execute a cross-server teleport
        plugin.getDatabase().setCurrentTeleport(teleporter, this);
        plugin.getMessenger().changeServer(teleporter, target.getServer());
        return CompletableFuture.completedFuture(TeleportResult.COMPLETED_CROSS_SERVER);
    }

    /**
     * Teleport a user not on this server but on the proxy network
     *
     * @return A {@link CompletableFuture} that completes when the teleport is finished.
     * Successful cross-server teleports will return {@link TeleportResult#COMPLETED_CROSS_SERVER}.
     * @implNote Cross-server teleports will return with a {@link TeleportResult#FAILED_INVALID_SERVER} result if the
     * target server is not online, or connection timed out
     */
    private CompletableFuture<TeleportResult> teleportNetworkedUser() {
        assert target != null;
        assert teleporter != null;

        // Send a network message to a user on another server to teleport them to the target position
        return Message.builder()
                .type(Message.Type.TELEPORT_TO_POSITION_REQUEST)
                .payload(Payload.withPosition(target))
                .target(teleporter.getUsername())
                .build().send(plugin.getMessenger(), executor)
                .thenApply(result -> Optional.ofNullable(result.getPayload().getResultState())
                        .orElse(TeleportResult.FAILED_TELEPORTER_NOT_RESOLVED));
    }

}
