package net.william278.huskhomes.teleport;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.messenger.EmptyPayload;
import net.william278.huskhomes.messenger.Message;
import net.william278.huskhomes.player.Player;
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
     * Teleport a {@link Player} to another {@link User}'s {@link Home}
     *
     * @param player    the {@link Player} to teleport
     * @param homeOwner the {@link User} who owns the home
     * @param homeName  the name of the home
     */
    public void teleportToHome(@NotNull Player player, @NotNull User homeOwner, @NotNull String homeName) {
        plugin.getDatabase().getHome(homeOwner, homeName).thenAccept(optionalHome ->
                optionalHome.ifPresentOrElse(home -> {
                    if (!homeOwner.uuid.equals(player.getUuid())) {
                        if (!home.isPublic && !player.hasPermission(Permission.COMMAND_HOME_OTHER.node)) {
                            plugin.getLocales().getLocale("error_public_home_invalid", homeOwner.username, homeName)
                                    .ifPresent(player::sendMessage);
                            return;
                        }
                    }
                    teleport(player, home).thenAccept(result -> finishTeleport(player, result));
                }, () -> {
                    if (homeOwner.uuid.equals(player.getUuid())) {
                        plugin.getLocales().getLocale("error_home_invalid", homeName).ifPresent(player::sendMessage);
                    } else {
                        plugin.getLocales().getLocale("error_home_invalid_other", homeName).ifPresent(player::sendMessage);
                    }
                }));
    }

    /**
     * Teleport a {@link Player} to a server {@link Warp}
     *
     * @param player   the {@link Player} to teleport
     * @param warpName the name of the warp
     */
    public void teleportToWarp(@NotNull Player player, @NotNull String warpName) {
        plugin.getDatabase().getWarp(warpName).thenAccept(optionalWarp ->
                optionalWarp.ifPresentOrElse(warp -> //todo permission restricted warps
                        teleport(player, warp).thenAccept(result -> finishTeleport(player, result)), () ->
                        plugin.getLocales().getLocale("error_warp_invalid", warpName)
                                .ifPresent(player::sendMessage)));
    }

    /**
     * Teleport a {@link Player} to another player by given input name
     *
     * @param player       the {@link Player} to teleport
     * @param targetPlayer the name of the target player
     */
    public void teleportToPlayer(@NotNull Player player, @NotNull String targetPlayer) {
        MatcherUtil.matchPlayerName(targetPlayer, plugin).ifPresentOrElse(playerName ->
                        getPlayerPosition(player, playerName).thenAccept(optionalPosition ->
                                optionalPosition.ifPresentOrElse(targetPosition ->
                                                teleport(player, targetPosition).thenAccept(result ->
                                                        finishTeleport(player, result)),
                                        () -> plugin.getLocales().getLocale("error_invalid_player").
                                                ifPresent(player::sendMessage))),
                () -> plugin.getLocales().getLocale("error_invalid_player").ifPresent(player::sendMessage));
    }

    /**
     * Gets the position of a player by their username, including players on other servers
     *
     * @param requester  the {@link Player} requesting their position
     * @param playerName the username of the player being requested
     * @return future optionally supplying the player's position, if the player could be found
     */
    private CompletableFuture<Optional<Position>> getPlayerPosition(@NotNull Player requester, @NotNull String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            final Optional<Player> localPlayer = plugin.getOnlinePlayers().stream().filter(player ->
                    player.getName().equalsIgnoreCase(playerName)).findFirst();
            if (localPlayer.isPresent()) {
                return localPlayer.get().getPosition().thenApply(Optional::of).join();
            }
            if (plugin.getSettings().getBooleanValue(Settings.ConfigOption.ENABLE_PROXY_MODE)) {
                assert plugin.getNetworkMessenger() != null;
                return plugin.getNetworkMessenger().sendMessage(requester,
                                new Message(Message.MessageType.POSITION_REQUEST,
                                        requester.getName(),
                                        playerName,
                                        new EmptyPayload(),
                                        Message.MessageKind.MESSAGE,
                                        plugin.getSettings().getIntegerValue(Settings.ConfigOption.CLUSTER_ID)))
                        .thenApply(reply -> Optional.of(Position.fromJson(reply.payload))).join();
            }
            return Optional.empty();
        });
    }

    /**
     * Teleport a {@link Player} to a specified {@link Position}. Respects timed teleport.
     *
     * @param player   the {@link Player} to teleport
     * @param position the target {@link Position} to teleport to
     */
    public CompletableFuture<TeleportResult> teleport(@NotNull Player player, @NotNull Position position) {
        return CompletableFuture.supplyAsync(() -> {
            final int teleportWarmupTime = plugin.getSettings().getIntegerValue(Settings.ConfigOption.TELEPORT_WARMUP_TIME);
            if (!player.hasPermission(Permission.BYPASS_TELEPORT_WARMUP.node) && teleportWarmupTime > 0) {
                return processTimedTeleport(new TimedTeleport(player, position, teleportWarmupTime))
                        .thenApply(teleport -> {
                            if (!teleport.cancelled) {
                                return teleport.getPlayer().teleport(teleport.getTargetPosition()).join();
                            } else {
                                return TeleportResult.CANCELLED;
                            }
                        }).join();
            } else {
                return teleportNow(player, position).join();
            }
        });
    }

    /**
     * Handles a completed {@link Player}'s {@link TeleportResult} with the appropriate message
     *
     * @param player         the {@link Player} who just completed a teleport
     * @param teleportResult the {@link TeleportResult} to handle
     */
    public void finishTeleport(@NotNull Player player, @NotNull TeleportResult teleportResult) {
        switch (teleportResult) {
            case COMPLETED_LOCALLY -> plugin.getLocales().getLocale("teleporting_complete")
                    .ifPresent(player::sendMessage);
            case FAILED_INVALID_WORLD -> plugin.getLocales().getLocale("error_invalid_on_arrival")
                    .ifPresent(player::sendMessage);
            case FAILED_UNSAFE -> {
                //todo
            }
            case FAILED_ILLEGAL_COORDINATES -> plugin.getLocales().getLocale("error_illegal_target_coordinates")
                    .ifPresent(player::sendMessage);
            case FAILED_INVALID_SERVER -> plugin.getLocales().getLocale("error_invalid_server")
                    .ifPresent(player::sendMessage);
        }
    }

    /**
     * Executes a teleport now, teleporting a {@link Player} to a specified {@link Position}
     *
     * @param player   the {@link Player} to teleport
     * @param position the target {@link Position} to teleport to
     */
    private CompletableFuture<TeleportResult> teleportNow(@NotNull Player player, @NotNull Position position) {
        final User user = new User(player);
        final Teleport teleport = new Teleport(user, position);
        return CompletableFuture.supplyAsync(() -> player.getPosition().thenApply(preTeleportPosition -> plugin.getDatabase()
                .setLastPosition(user, preTeleportPosition) // Update the player's last position
                .thenApply(ignored -> plugin.getServer(player).thenApply(server -> {
                    // Teleport player locally, or across server depending on need
                    if (position.server.equals(server)) {
                        return player.teleport(teleport.target).join();
                    } else {
                        return teleportCrossServer(player, teleport).join();
                    }
                }).join()).join()).join());
    }

    private CompletableFuture<TeleportResult> teleportCrossServer(Player player, Teleport teleport) {
        assert plugin.getNetworkMessenger() != null;
        return CompletableFuture.supplyAsync(() -> plugin.getDatabase().setCurrentTeleport(teleport.player, teleport)
                .thenApply(ignored -> plugin.getNetworkMessenger().sendPlayer(player, teleport.target.server)
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
            teleport.cancelled = true;
            return Optional.of(teleport);
        }

        // Cancel the timed teleport if the player moves
        if (teleport.hasMoved()) {
            plugin.getLocales().getLocale("teleporting_cancelled_movement").ifPresent(locale ->
                    teleport.getPlayer().sendMessage(locale));
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
