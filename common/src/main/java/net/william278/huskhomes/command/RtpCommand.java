package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.UserData;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportBuilder;
import net.william278.huskhomes.util.Permission;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class RtpCommand extends CommandBase implements ConsoleExecutable {

    protected RtpCommand(@NotNull HuskHomes implementor) {
        super("rtp", "[player]", Permission.COMMAND_RTP, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        OnlineUser target = onlineUser;
        if (args.length >= 1) {
            if (!onlineUser.hasPermission(Permission.COMMAND_RTP_OTHER.node)) {
                plugin.getLocales().getLocale("error_no_permission").ifPresent(onlineUser::sendMessage);
                return;
            }
            final Optional<OnlineUser> foundUser = plugin.findOnlinePlayer(args[0]);
            if (foundUser.isEmpty()) {
                plugin.getLocales().getLocale("error_player_not_found", args[0])
                        .ifPresent(onlineUser::sendMessage);
                return;
            }
            target = foundUser.get();
        }
        final Position userPosition = target.getPosition();
        final String[] rtpArguments = args.length >= 1 ? ArrayUtils.subarray(args, 1, args.length) : args;
        if (plugin.getSettings().rtpRestrictedWorlds.stream()
                .anyMatch(worldName -> worldName.equals(userPosition.world.name))) {
            plugin.getLocales().getLocale("error_rtp_restricted_world")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        // Perform economy check if necessary
        if (!plugin.validateEconomyCheck(onlineUser, Settings.EconomyAction.RANDOM_TELEPORT)) {
            return;
        }

        final OnlineUser userToTeleport = target;
        final boolean isExecutorTeleporting = userToTeleport.uuid.equals(onlineUser.uuid);
        plugin.getDatabase().getUserData(onlineUser.uuid).thenAccept(userData -> {
            // Check the user is not still on /rtp cooldown
            if (userData.isEmpty()) {
                return;
            }
            final Instant currentTime = Instant.now();
            if (isExecutorTeleporting && !currentTime.isAfter(userData.get().rtpCooldown())
                && !onlineUser.hasPermission(Permission.BYPASS_RTP_COOLDOWN.node)) {
                plugin.getLocales().getLocale("error_rtp_cooldown",
                                Long.toString(currentTime.until(userData.get().rtpCooldown(), ChronoUnit.MINUTES) + 1))
                        .ifPresent(onlineUser::sendMessage);
                return;
            }

            // Get a random position and teleport
            plugin.getLocales().getLocale("teleporting_random_generation")
                    .ifPresent(onlineUser::sendMessage);
            plugin.getRandomTeleportEngine().getRandomPosition(onlineUser.getPosition().world, rtpArguments).thenAccept(position -> {
                if (position.isEmpty()) {
                    plugin.getLocales().getLocale("error_rtp_randomization_timeout")
                            .ifPresent(onlineUser::sendMessage);
                    return;
                }

                final TeleportBuilder builder = Teleport.builder(plugin, userToTeleport)
                        .setTarget(position.get());
                final CompletableFuture<? extends Teleport> teleportFuture = isExecutorTeleporting
                        ? builder.setEconomyActions(Settings.EconomyAction.RANDOM_TELEPORT).toTimedTeleport()
                        : builder.toTeleport();

                teleportFuture.thenAccept(teleport -> teleport.execute()
                        .thenAccept(result -> {
                            if (isExecutorTeleporting &&
                                result.successful() && !onlineUser.hasPermission(Permission.BYPASS_RTP_COOLDOWN.node)) {
                                plugin.getDatabase().updateUserData(new UserData(onlineUser,
                                        userData.get().homeSlots(), userData.get().ignoringTeleports(),
                                        Instant.now().plus(plugin.getSettings().rtpCooldownLength, ChronoUnit.MINUTES)));
                            }
                        }));
            });
        });
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        if (args.length == 0) {
            plugin.getLoggingAdapter().log(Level.WARNING, "Invalid syntax. Usage: rtp [player]");
            return;
        }
        final Optional<OnlineUser> foundUser = plugin.findOnlinePlayer(args[0]);
        if (foundUser.isEmpty()) {
            plugin.getLoggingAdapter().log(Level.WARNING, "Player not found: " + args[0]);
            return;
        }

        plugin.getLoggingAdapter().log(Level.INFO, "Finding a random position for " + foundUser.get().username + "...");
        plugin.getRandomTeleportEngine().getRandomPosition(foundUser.get().getPosition().world, ArrayUtils.subarray(args, 1, args.length)).thenAccept(position -> {
            if (position.isEmpty()) {
                plugin.getLoggingAdapter().log(Level.WARNING, "Failed to teleport " + foundUser.get().username + " to a random position; randomization timed out!");
                return;
            }
            Teleport.builder(plugin, foundUser.get())
                    .setTarget(position.get())
                    .toTeleport()
                    .thenAccept(teleport -> teleport.execute().thenAccept(result -> {
                        if (result.successful()) {
                            plugin.getLoggingAdapter().log(Level.INFO, "Teleported " + foundUser.get().username + " to a random position.");
                        } else {
                            plugin.getLoggingAdapter().log(Level.WARNING, "Failed to teleport" + foundUser.get().username + " to a random position.");
                        }
                    }));
        });

    }
}
