package net.william278.huskhomes.teleport;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.messenger.EmptyPayload;
import net.william278.huskhomes.messenger.Message;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.util.MatcherUtil;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Cross-platform teleportation manager
 */
public abstract class TeleportManager {

    /**
     * Instance of the implementing plugin
     */
    @NotNull
    protected final HuskHomes plugin;

    public TeleportManager(@NotNull HuskHomes implementor) {
        this.plugin = implementor;
    }

    /**
     * Teleport a {@link OnlineUser} to another {@link User}'s {@link Home}
     *
     * @param onlineUser the {@link OnlineUser} to teleport
     * @param homeOwner  the {@link User} who owns the home
     * @param homeName   the name of the home
     */
    public void teleportToHome(@NotNull OnlineUser onlineUser, @NotNull User homeOwner, @NotNull String homeName) {
        plugin.getDatabase().getHome(homeOwner, homeName).thenAccept(optionalHome ->
                optionalHome.ifPresentOrElse(home -> {
                    if (!homeOwner.uuid.equals(onlineUser.uuid)) {
                        if (!home.isPublic && !onlineUser.hasPermission(Permission.COMMAND_HOME_OTHER.node)) {
                            plugin.getLocales().getLocale("error_public_home_invalid", homeOwner.username, homeName)
                                    .ifPresent(onlineUser::sendMessage);
                            return;
                        }
                    }
                    teleport(onlineUser, home).thenAccept(result -> finishTeleport(onlineUser, result));
                }, () -> {
                    if (homeOwner.uuid.equals(onlineUser.uuid)) {
                        plugin.getLocales().getLocale("error_home_invalid", homeName).ifPresent(onlineUser::sendMessage);
                    } else {
                        plugin.getLocales().getLocale("error_home_invalid_other", homeName).ifPresent(onlineUser::sendMessage);
                    }
                }));
    }

    /**
     * Teleport a {@link OnlineUser} to a server {@link Warp}
     *
     * @param onlineUser the {@link OnlineUser} to teleport
     * @param warpName   the name of the warp
     */
    public void teleportToWarp(@NotNull OnlineUser onlineUser, @NotNull String warpName) {
        plugin.getDatabase().getWarp(warpName).thenAccept(optionalWarp ->
                optionalWarp.ifPresentOrElse(warp -> //todo permission restricted warps
                        teleport(onlineUser, warp).thenAccept(result -> finishTeleport(onlineUser, result)), () ->
                        plugin.getLocales().getLocale("error_warp_invalid", warpName)
                                .ifPresent(onlineUser::sendMessage)));
    }

    /**
     * Teleport a {@link OnlineUser} to another player by given input name
     *
     * @param onlineUser   the {@link OnlineUser} to teleport
     * @param targetPlayer the name of the target player
     */
    public void teleportToPlayer(@NotNull OnlineUser onlineUser, @NotNull String targetPlayer) {
        MatcherUtil.matchPlayerName(targetPlayer, plugin).ifPresentOrElse(playerName ->
                        getPlayerPosition(onlineUser, playerName).thenAccept(optionalPosition ->
                                optionalPosition.ifPresentOrElse(targetPosition ->
                                                teleport(onlineUser, targetPosition).thenAccept(result ->
                                                        finishTeleport(onlineUser, result)),
                                        () -> plugin.getLocales().getLocale("error_invalid_player").
                                                ifPresent(onlineUser::sendMessage))),
                () -> plugin.getLocales().getLocale("error_invalid_player").ifPresent(onlineUser::sendMessage));
    }

    /**
     * Gets the position of a player by their username, including players on other servers
     *
     * @param requester  the {@link OnlineUser} requesting their position
     * @param playerName the username of the player being requested
     * @return future optionally supplying the player's position, if the player could be found
     */
    private CompletableFuture<Optional<Position>> getPlayerPosition(@NotNull OnlineUser requester, @NotNull String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            final Optional<OnlineUser> localPlayer = plugin.getOnlinePlayers().stream().filter(player ->
                    player.username.equalsIgnoreCase(playerName)).findFirst();
            if (localPlayer.isPresent()) {
                return localPlayer.get().getPosition().thenApply(Optional::of).join();
            }
            if (plugin.getSettings().crossServer) {
                assert plugin.getNetworkMessenger() != null;
                return plugin.getNetworkMessenger().sendMessage(requester,
                                new Message(Message.MessageType.POSITION_REQUEST,
                                        requester.username,
                                        playerName,
                                        new EmptyPayload(),
                                        Message.MessageKind.MESSAGE,
                                        plugin.getSettings().clusterId))
                        .thenApply(reply -> Optional.of(Position.fromJson(reply.payload))).join();
            }
            return Optional.empty();
        });
    }

    /**
     * Teleport a {@link OnlineUser} to a specified {@link Position}. Respects timed teleport.
     *
     * @param onlineUser the {@link OnlineUser} to teleport
     * @param position   the target {@link Position} to teleport to
     */
    public CompletableFuture<TeleportResult> teleport(@NotNull OnlineUser onlineUser, @NotNull Position position) {
        return CompletableFuture.supplyAsync(() -> {
            final int teleportWarmupTime = plugin.getSettings().teleportWarmupTime;
            if (!onlineUser.hasPermission(Permission.BYPASS_TELEPORT_WARMUP.node) && teleportWarmupTime > 0) {
                return processTimedTeleport(new TimedTeleport(onlineUser, position, teleportWarmupTime))
                        .thenApply(teleport -> {
                            if (!teleport.cancelled) {
                                return teleportNow(onlineUser, position).join();
                            } else {
                                return TeleportResult.CANCELLED;
                            }
                        }).join();
            } else {
                return teleportNow(onlineUser, position).join();
            }
        });
    }

    /**
     * Handles a completed {@link OnlineUser}'s {@link TeleportResult} with the appropriate message
     *
     * @param onlineUser     the {@link OnlineUser} who just completed a teleport
     * @param teleportResult the {@link TeleportResult} to handle
     */
    public void finishTeleport(@NotNull OnlineUser onlineUser, @NotNull TeleportResult teleportResult) {
        switch (teleportResult) {
            case COMPLETED_LOCALLY -> plugin.getLocales().getLocale("teleporting_complete")
                    .ifPresent(onlineUser::sendMessage);
            case FAILED_INVALID_WORLD -> plugin.getLocales().getLocale("error_invalid_on_arrival")
                    .ifPresent(onlineUser::sendMessage);
            case FAILED_UNSAFE -> {
                //todo
            }
            case FAILED_ILLEGAL_COORDINATES -> plugin.getLocales().getLocale("error_illegal_target_coordinates")
                    .ifPresent(onlineUser::sendMessage);
            case FAILED_INVALID_SERVER -> plugin.getLocales().getLocale("error_invalid_server")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    /**
     * Executes a teleport now, teleporting a {@link OnlineUser} to a specified {@link Position}
     *
     * @param onlineUser the {@link OnlineUser} to teleport
     * @param position   the target {@link Position} to teleport to
     */
    private CompletableFuture<TeleportResult> teleportNow(@NotNull OnlineUser onlineUser, @NotNull Position position) {
        final Teleport teleport = new Teleport(onlineUser, position);
        return CompletableFuture.supplyAsync(() -> onlineUser.getPosition().thenApply(preTeleportPosition -> plugin.getDatabase()
                .setLastPosition(onlineUser, preTeleportPosition) // Update the player's last position
                .thenApply(ignored -> plugin.getServer(onlineUser).thenApply(server -> {
                    // Teleport player locally, or across server depending on need
                    if (position.server.equals(server)) {
                        return onlineUser.teleport(teleport.target).join();
                    } else {
                        return teleportCrossServer(onlineUser, teleport).join();
                    }
                }).join()).join()).join());
    }

    private CompletableFuture<TeleportResult> teleportCrossServer(OnlineUser onlineUser, Teleport teleport) {
        assert plugin.getNetworkMessenger() != null;
        return CompletableFuture.supplyAsync(() -> plugin.getDatabase().setCurrentTeleport(teleport.player, teleport)
                .thenApply(ignored -> plugin.getNetworkMessenger().sendPlayer(onlineUser, teleport.target.server)
                        .thenApply(completed -> completed ? TeleportResult.COMPLETED_CROSS_SERVER :
                                TeleportResult.FAILED_INVALID_SERVER)
                        .join()).join());
    }

    /**
     * Process a timed teleport, ticking it
     *
     * @param teleport the {@link TimedTeleport} being ticked
     * @return {@code true} if the implementor should cancel the timedTeleport
     */
    protected final Optional<TimedTeleport> tickTimedTeleport(@NotNull final TimedTeleport teleport) {
        if (teleport.isDone()) {
            return Optional.of(teleport);
        }

        // Cancel the timed teleport if the player takes damage
        if (teleport.hasTakenDamage()) {
            plugin.getLocales().getLocale("teleporting_cancelled_damage").ifPresent(locale ->
                    teleport.getPlayer().sendMessage(locale));
            plugin.getLocales().getLocale("teleporting_action_bar_cancelled").ifPresent(locale ->
                    teleport.getPlayer().sendActionBar(locale));
            teleport.cancelled = true;
            return Optional.of(teleport);
        }

        // Cancel the timed teleport if the player moves
        if (teleport.hasMoved()) {
            plugin.getLocales().getLocale("teleporting_cancelled_movement").ifPresent(locale ->
                    teleport.getPlayer().sendMessage(locale));
            plugin.getLocales().getLocale("teleporting_action_bar_cancelled").ifPresent(locale ->
                    teleport.getPlayer().sendActionBar(locale));
            teleport.cancelled = true;
            return Optional.of(teleport);
        }

        // Decrement the countdown timer
        teleport.countDown();
        return Optional.empty();
    }

    /**
     * Process a timed teleport, implemented by platform-specific schedulers
     *
     * @param teleport the {@link TimedTeleport} to process
     * @return a future, returning when the teleport has finished
     */
    public abstract CompletableFuture<TimedTeleport> processTimedTeleport(TimedTeleport teleport);

}
