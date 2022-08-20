package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.UserData;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RtpCommand extends CommandBase implements ConsoleExecutable {

    protected RtpCommand(@NotNull HuskHomes implementor) {
        super("rtp", Permission.COMMAND_RTP, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length != 0) {
            plugin.getLocales().getLocale("error_invalid_syntax", "/rtp")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }
        final Position userPosition = onlineUser.getPosition().join();
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

        CompletableFuture.runAsync(() -> {
            // Check the user is not still on /rtp cooldown
            final Optional<UserData> userData = plugin.getDatabase().getUserData(onlineUser.uuid).join();
            if (userData.isPresent()) {
                final Instant currentTime = Instant.now();
                if (!currentTime.isAfter(userData.get().rtpCooldown())) {
                    plugin.getLocales().getLocale("error_rtp_cooldown",
                                    Long.toString(currentTime.until(userData.get().rtpCooldown(), ChronoUnit.MINUTES)))
                            .ifPresent(onlineUser::sendMessage);
                    return;
                }
            }

            // Get a random position and teleport
            plugin.getRtpEngine().getRandomPosition(userPosition)
                    .thenAccept(position -> {
                        if (position.isEmpty()) {
                            plugin.getLocales().getLocale("error_rtp_randomization_timeout")
                                    .ifPresent(onlineUser::sendMessage);
                            return;
                        }
                        plugin.getTeleportManager().timedTeleport(onlineUser, position.get(), Settings.EconomyAction.RANDOM_TELEPORT)
                                .thenAccept(result -> plugin.getTeleportManager()
                                        .finishTeleport(onlineUser, result, Settings.EconomyAction.RANDOM_TELEPORT)).join();
                    }).join();
        });
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }
}
