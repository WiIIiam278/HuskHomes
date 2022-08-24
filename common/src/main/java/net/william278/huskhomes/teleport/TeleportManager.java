package net.william278.huskhomes.teleport;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.messenger.Message;
import net.william278.huskhomes.messenger.MessagePayload;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Cross-platform teleportation manager
 */
public class TeleportManager {

    /**
     * Instance of the implementing plugin
     */
    @NotNull
    protected final HuskHomes plugin;

    /**
     * A set of user UUIDs currently on warmup countdowns for {@link TimedTeleport}
     */
    @NotNull
    private final HashSet<UUID> currentlyOnWarmup = new HashSet<>();

    public TeleportManager(@NotNull HuskHomes implementor) {
        this.plugin = implementor;
    }

    /**
     * Attempt to teleport a {@link OnlineUser} to a {@link User}'s home by the home name
     *
     * @param onlineUser the {@link OnlineUser} to teleport
     * @param homeOwner  the {@link User} who owns the home
     * @param homeName   the name of the home
     */
    public void teleportToHomeByName(@NotNull OnlineUser onlineUser, @NotNull User homeOwner, @NotNull String homeName) {
        plugin.getDatabase().getHome(homeOwner, homeName).thenAccept(optionalHome ->
                optionalHome.ifPresentOrElse(home -> teleportToHome(onlineUser, home), () -> {
                    if (homeOwner.uuid.equals(onlineUser.uuid)) {
                        plugin.getLocales().getLocale("error_home_invalid", homeName).ifPresent(onlineUser::sendMessage);
                    } else {
                        plugin.getLocales().getLocale("error_home_invalid_other", homeName).ifPresent(onlineUser::sendMessage);
                    }
                }));
    }

    /**
     * Attempt to teleport a {@link OnlineUser} to a {@link Home}
     *
     * @param onlineUser the {@link OnlineUser} to teleport
     * @param home       the {@link Home} to teleport to
     */
    public void teleportToHome(@NotNull OnlineUser onlineUser, @NotNull Home home) {
        if (!home.owner.uuid.equals(onlineUser.uuid)) {
            if (!home.isPublic && !onlineUser.hasPermission(Permission.COMMAND_HOME_OTHER.node)) {
                plugin.getLocales().getLocale("error_public_home_invalid", home.owner.username, home.meta.name)
                        .ifPresent(onlineUser::sendMessage);
                return;
            }
        }
        timedTeleport(onlineUser, home).thenAccept(result -> finishTeleport(onlineUser, result));
    }

    /**
     * Attempt to teleport a {@link OnlineUser} to a server warp by its' given name
     *
     * @param onlineUser the {@link OnlineUser} to teleport
     * @param warpName   the name of the warp
     */
    public void teleportToWarpByName(@NotNull OnlineUser onlineUser, @NotNull String warpName) {
        plugin.getDatabase().getWarp(warpName).thenAccept(optionalWarp ->
                optionalWarp.ifPresentOrElse(warp ->
                        teleportToWarp(onlineUser, warp), () ->
                        plugin.getLocales().getLocale("error_warp_invalid", warpName)
                                .ifPresent(onlineUser::sendMessage)));
    }

    /**
     * Attempt to teleport a {@link OnlineUser} to a server {@link Warp}.
     * <p>
     * If permission restricted warps are enabled, the user will not be teleported if they lack the
     * required permission node ({@code huskhomes.warp.[warp_name]})
     *
     * @param onlineUser the {@link OnlineUser} to teleport
     * @param warp       the {@link Warp} to teleport to
     */
    public void teleportToWarp(@NotNull OnlineUser onlineUser, @NotNull Warp warp) {
        // Check against warp permission restrictions if enabled (huskhomes.warp.<warp_name>)
        if (plugin.getSettings().permissionRestrictWarps) {
            if (!onlineUser.hasPermission(warp.getPermissionNode())) {
                plugin.getLocales().getLocale("error_permission_restricted_warp", warp.meta.name)
                        .ifPresent(onlineUser::sendMessage);
                return;
            }
        }

        timedTeleport(onlineUser, warp).thenAccept(result -> finishTeleport(onlineUser, result));
    }

    /**
     * Teleport a {@link OnlineUser} to another player by username
     *
     * @param onlineUser   the {@link OnlineUser} to teleport
     * @param targetPlayer the name of the target player
     */
    public void teleportToPlayerByName(@NotNull OnlineUser onlineUser, @NotNull String targetPlayer) {
        CompletableFuture.runAsync(() -> {
            final Optional<OnlineUser> localPlayer = plugin.findPlayer(targetPlayer);
            if (localPlayer.isPresent()) {
                timedTeleport(onlineUser, localPlayer.get().getPosition()).thenAccept(
                        result -> finishTeleport(onlineUser, result));
            } else if (plugin.getSettings().crossServer) {
                assert plugin.getNetworkMessenger() != null;
                getPlayerPositionByName(onlineUser, targetPlayer).thenAccept(optionalPosition -> {
                    if (optionalPosition.isPresent()) {
                        timedTeleport(onlineUser, optionalPosition.get())
                                .thenAccept(teleportResult -> finishTeleport(onlineUser, teleportResult));
                        return;
                    }
                    plugin.getLocales().getLocale("error_player_not_found", targetPlayer)
                            .ifPresent(onlineUser::sendMessage);
                });
            } else {
                plugin.getLocales().getLocale("error_player_not_found", targetPlayer)
                        .ifPresent(onlineUser::sendMessage);
            }
        });
    }

    /**
     * Immediately teleport a player by username to a {@link Position} by name
     *
     * @param playerName username of the target player to teleport
     * @param position   the {@link Position} to teleport to
     * @param requester  the {@link OnlineUser} performing the teleport action
     * @return a {@link CompletableFuture} that completes when the teleport is complete with the {@link TeleportResult},
     * if it was processed, otherwise an empty {@link Optional} if the player was not found
     */
    public CompletableFuture<Optional<TeleportResult>> teleportPlayerByName(@NotNull String playerName, @NotNull Position position,
                                                                            @NotNull OnlineUser requester) {
        final Optional<OnlineUser> localPlayer = plugin.findPlayer(playerName);
        if (localPlayer.isPresent()) {
            return teleport(localPlayer.get(), position).thenApply(Optional::of);
        }
        if (plugin.getSettings().crossServer && plugin.getNetworkMessenger() != null) {
            return plugin.getNetworkMessenger().sendMessage(requester,
                            new Message(Message.MessageType.TELEPORT_TO_POSITION_REQUEST,
                                    requester.username,
                                    playerName,
                                    MessagePayload.withPosition(position),
                                    Message.RelayType.MESSAGE,
                                    plugin.getSettings().clusterId))
                    .thenApply(result -> {
                        if (result.payload.teleportResult == null) {
                            return Optional.empty();
                        }
                        return Optional.of(result.payload.teleportResult);
                    });
        }
        return CompletableFuture.supplyAsync(Optional::empty);
    }

    /**
     * Teleport two players by username
     *
     * @param playerName   the name of the player to teleport
     * @param targetPlayer the name of the target player
     * @param requester    the {@link OnlineUser} performing the teleport action
     * @return a {@link CompletableFuture} that completes when the teleport is complete with the {@link TeleportResult},
     */
    public CompletableFuture<Optional<TeleportResult>> teleportPlayerToPlayerByName(@NotNull String playerName,
                                                                                    @NotNull String targetPlayer,
                                                                                    @NotNull OnlineUser requester) {
        final Optional<Position> localPositionTarget = plugin.findPlayer(targetPlayer).map(OnlineUser::getPosition);
        if (localPositionTarget.isPresent()) {
            return teleportPlayerByName(playerName, localPositionTarget.get(), requester);
        }
        return getPlayerPositionByName(requester, targetPlayer).thenApply(position -> {
            if (position.isEmpty()) {
                return Optional.empty();
            }
            return teleportPlayerByName(playerName, position.get(), requester).join();
        });
    }

    /**
     * Gets the position of a player by their username, including players on other servers
     *
     * @param requester  the {@link OnlineUser} requesting their position
     * @param playerName the username of the player being requested
     * @return future optionally supplying the player's position, if the player could be found
     */
    private CompletableFuture<Optional<Position>> getPlayerPositionByName(@NotNull OnlineUser requester,
                                                                          @NotNull String playerName) {
        final Optional<OnlineUser> localPlayer = plugin.findPlayer(playerName);
        if (localPlayer.isPresent()) {
            return CompletableFuture.supplyAsync(() -> Optional.of(localPlayer.get().getPosition()));
        }
        if (plugin.getSettings().crossServer) {
            assert plugin.getNetworkMessenger() != null;
            return plugin.getNetworkMessenger().findPlayer(requester, playerName).thenApply(foundPlayer -> {
                if (foundPlayer.isEmpty()) {
                    return Optional.empty();
                }
                return plugin.getNetworkMessenger().sendMessage(requester,
                                new Message(Message.MessageType.POSITION_REQUEST,
                                        requester.username,
                                        playerName,
                                        MessagePayload.empty(),
                                        Message.RelayType.MESSAGE,
                                        plugin.getSettings().clusterId))
                        .orTimeout(3, TimeUnit.SECONDS)
                        .exceptionally(throwable -> null)
                        .thenApply(reply -> Optional.ofNullable(reply.payload.position)).join();
            });
        }
        return CompletableFuture.supplyAsync(Optional::empty);
    }

    /**
     * Teleport a {@link OnlineUser} to a specified {@link Position} after a warmup period
     *
     * @param onlineUser     the {@link OnlineUser} to teleport
     * @param position       the target {@link Position} to teleport to
     * @param economyActions any economy actions to validate before the teleport.
     *                       Note that this will not actually carry out the economy transactions, only validate them
     */
    public CompletableFuture<TeleportResult> timedTeleport(@NotNull OnlineUser onlineUser, @NotNull Position position,
                                                           @NotNull Settings.EconomyAction... economyActions) {
        // Prevent players starting multiple timed teleports
        if (currentlyOnWarmup.contains(onlineUser.uuid)) {
            return CompletableFuture.supplyAsync(() -> TeleportResult.FAILED_ALREADY_TELEPORTING);
        }

        final int teleportWarmupTime = plugin.getSettings().teleportWarmupTime;
        if (!onlineUser.hasPermission(Permission.BYPASS_TELEPORT_WARMUP.node) && teleportWarmupTime > 0) {
            return CompletableFuture.supplyAsync(() -> processTeleportWarmup(new TimedTeleport(onlineUser, position, teleportWarmupTime))
                    .thenApply(teleport -> {
                        if (!teleport.cancelled) {
                            for (final Settings.EconomyAction action : economyActions) {
                                if (!plugin.validateEconomyCheck(onlineUser, action)) {
                                    return TeleportResult.CANCELLED;
                                }
                            }
                            return teleport(onlineUser, position).join();
                        } else {
                            return TeleportResult.CANCELLED;
                        }
                    }).join());
        } else {
            return teleport(onlineUser, position);
        }
    }

    /**
     * Handles a completed {@link OnlineUser}'s {@link TeleportResult} with the appropriate message
     *
     * @param onlineUser     the {@link OnlineUser} to send the teleport completion message to
     * @param teleportResult the {@link TeleportResult} to handle
     * @param economyActions any economy actions to complete the transaction of
     */
    public void finishTeleport(@NotNull OnlineUser onlineUser, @NotNull TeleportResult teleportResult,
                               @NotNull Settings.EconomyAction... economyActions) {
        // Display the teleport result message
        switch (teleportResult) {
            case COMPLETED_LOCALLY -> plugin.getLocales().getLocale("teleporting_complete")
                    .ifPresent(onlineUser::sendMessage);
            case FAILED_ALREADY_TELEPORTING -> plugin.getLocales().getLocale("error_already_teleporting")
                    .ifPresent(onlineUser::sendMessage);
            case FAILED_INVALID_WORLD -> plugin.getLocales().getLocale("error_invalid_on_arrival")
                    .ifPresent(onlineUser::sendMessage);
            case FAILED_ILLEGAL_COORDINATES -> plugin.getLocales().getLocale("error_illegal_target_coordinates")
                    .ifPresent(onlineUser::sendMessage);
            case FAILED_INVALID_SERVER -> plugin.getLocales().getLocale("error_invalid_server")
                    .ifPresent(onlineUser::sendMessage);
        }

        // If the result was successful
        if (teleportResult.successful) {
            // Play sound
            plugin.getSettings().getSoundEffect(Settings.SoundEffectAction.TELEPORTATION_COMPLETE)
                    .ifPresent(onlineUser::playSound);

            // Handle economy actions
            for (final Settings.EconomyAction action : economyActions) {
                plugin.performEconomyTransaction(onlineUser, action);
            }
        }
    }

    /**
     * Carries out a teleport, teleporting a {@link OnlineUser} to a specified {@link Position} with a TeleportType of
     * {@link TeleportType#TELEPORT} and returning a future that will return a {@link TeleportResult}
     *
     * @param onlineUser the {@link OnlineUser} to teleport
     * @param position   the target {@link Position} to teleport to
     * @return a {@link CompletableFuture} that completes when the teleport is complete with the {@link TeleportResult}
     */
    public CompletableFuture<TeleportResult> teleport(@NotNull OnlineUser onlineUser, @NotNull Position position) {
        return teleport(onlineUser, position, TeleportType.TELEPORT);
    }

    /**
     * Carries out a teleport, teleporting a {@link OnlineUser} to a specified {@link Position} and returning
     * a future that will return a {@link TeleportResult}
     *
     * @param onlineUser   the {@link OnlineUser} to teleport
     * @param position     the target {@link Position} to teleport to
     * @param teleportType the {@link TeleportType} of the teleport
     * @return a {@link CompletableFuture} that completes when the teleport is complete with the {@link TeleportResult}
     */
    public CompletableFuture<TeleportResult> teleport(@NotNull OnlineUser onlineUser, @NotNull Position position,
                                                      @NotNull TeleportType teleportType) {
        final Teleport teleport = new Teleport(onlineUser, position, teleportType);

        // Update the player's last position
        if (!plugin.getSettings().backCommandSaveOnTeleportEvent) {
            plugin.getDatabase().setLastPosition(onlineUser, onlineUser.getPosition());
        }

        // Teleport player locally, or across server depending on need
        if (position.server.equals(plugin.getServer(onlineUser))) {
            return onlineUser.teleport(teleport.target, plugin.getSettings().asynchronousTeleports);
        } else {
            return teleportCrossServer(onlineUser, teleport);
        }
    }

    /**
     * Handles a cross-server teleport, setting database parameters and dispatching a player across the network
     *
     * @param onlineUser the {@link OnlineUser} to teleport
     * @param teleport   the {@link Teleport} to carry out
     * @return future completing when the teleport is complete with a {@link TeleportResult}.
     * Successful cross-server teleports will return {@link TeleportResult#COMPLETED_CROSS_SERVER}.
     * <p>Note that cross-server teleports will return with a {@link TeleportResult#FAILED_INVALID_SERVER} result if the
     * target server is not online
     */
    private CompletableFuture<TeleportResult> teleportCrossServer(@NotNull OnlineUser onlineUser, @NotNull Teleport teleport) {
        assert plugin.getNetworkMessenger() != null;
        return plugin.getDatabase().setCurrentTeleport(teleport.player, teleport)
                .thenApply(ignored -> plugin.getNetworkMessenger().sendPlayer(onlineUser, teleport.target.server)
                        .thenApply(completed -> completed ? TeleportResult.COMPLETED_CROSS_SERVER :
                                TeleportResult.FAILED_INVALID_SERVER)
                        .join());
    }

    /**
     * Processes a timed teleport
     *
     * @param teleport the {@link TimedTeleport} to process
     * @return a future, returning when the teleport has finished
     */
    private CompletableFuture<TimedTeleport> processTeleportWarmup(@NotNull final TimedTeleport teleport) {
        // Mark the player as warming up
        currentlyOnWarmup.add(teleport.getPlayer().uuid);

        // Create a scheduled executor to tick the timed teleport
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        final CompletableFuture<TimedTeleport> timedTeleportFuture = new CompletableFuture<>();
        executor.scheduleAtFixedRate(() -> {
            // Display countdown action bar message
            if (teleport.timeLeft > 0) {
                plugin.getSettings().getSoundEffect(Settings.SoundEffectAction.TELEPORTATION_WARMUP)
                        .ifPresent(sound -> teleport.getPlayer().playSound(sound));
                plugin.getLocales().getLocale("teleporting_action_bar_countdown", Integer.toString(teleport.timeLeft))
                        .ifPresent(message -> {
                            switch (plugin.getSettings().teleportWarmupDisplay) {
                                case ACTION_BAR -> teleport.getPlayer().sendActionBar(message);
                                case MESSAGE -> teleport.getPlayer().sendMessage(message);
                            }
                        });
            } else {
                plugin.getLocales().getLocale("teleporting_complete")
                        .ifPresent(message -> {
                            switch (plugin.getSettings().teleportWarmupDisplay) {
                                case ACTION_BAR -> teleport.getPlayer().sendActionBar(message);
                                case MESSAGE -> teleport.getPlayer().sendMessage(message);
                            }
                        });
            }

            // Tick (decrement) the timed teleport timer
            final Optional<TimedTeleport> result = tickTeleportWarmup(teleport);
            if (result.isPresent()) {
                currentlyOnWarmup.remove(teleport.getPlayer().uuid);
                timedTeleportFuture.complete(teleport);
                executor.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);
        return timedTeleportFuture;
    }

    /**
     * Ticks a timed teleport, decrementing the time left until the teleport is complete
     * <p>
     * A timed teleport will be cancelled if certain criteria are met:
     * <ul>
     *     <li>The player has left the server</li>
     *     <li>The plugin is disabling</li>
     *     <li>The player has moved beyond the movement threshold from when the warmup started</li>
     *     <li>The player has taken damage (though they may heal, have status ailments or lose/gain hunger)</li>
     * </ul>
     *
     * @param teleport the {@link TimedTeleport} being ticked
     * @return Optional containing the {@link TimedTeleport} after it has been ticked,
     * or {@link Optional#empty()} if the teleport has been cancelled
     */
    private Optional<TimedTeleport> tickTeleportWarmup(@NotNull final TimedTeleport teleport) {
        if (teleport.isDone()) {
            return Optional.of(teleport);
        }

        // Cancel the timed teleport if the player takes damage
        if (teleport.hasTakenDamage()) {
            plugin.getLocales().getLocale("teleporting_cancelled_damage").ifPresent(locale ->
                    teleport.getPlayer().sendMessage(locale));
            plugin.getLocales().getLocale("teleporting_action_bar_cancelled").ifPresent(locale ->
                    teleport.getPlayer().sendActionBar(locale));
            plugin.getSettings().getSoundEffect(Settings.SoundEffectAction.TELEPORTATION_CANCELLED)
                    .ifPresent(sound -> teleport.getPlayer().playSound(sound));
            teleport.cancelled = true;
            return Optional.of(teleport);
        }

        // Cancel the timed teleport if the player moves
        if (teleport.hasMoved()) {
            plugin.getLocales().getLocale("teleporting_cancelled_movement").ifPresent(locale ->
                    teleport.getPlayer().sendMessage(locale));
            plugin.getLocales().getLocale("teleporting_action_bar_cancelled").ifPresent(locale ->
                    teleport.getPlayer().sendActionBar(locale));
            plugin.getSettings().getSoundEffect(Settings.SoundEffectAction.TELEPORTATION_CANCELLED)
                    .ifPresent(sound -> teleport.getPlayer().playSound(sound));
            teleport.cancelled = true;
            return Optional.of(teleport);
        }

        // Decrement the countdown timer
        teleport.countDown();
        return Optional.empty();
    }

}
