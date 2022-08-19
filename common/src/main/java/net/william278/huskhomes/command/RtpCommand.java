package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

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

        if (!plugin.validateEconomyCheck(onlineUser, Settings.EconomyAction.RANDOM_TELEPORT)) {
            return;
        }

        plugin.getRtpEngine().getRandomPosition(userPosition)
                .thenAccept(position -> {
                    if (position.isEmpty()) {
                        plugin.getLocales().getLocale("error_rtp_randomization_timeout")
                                .ifPresent(onlineUser::sendMessage);
                        return;
                    }
                    plugin.getTeleportManager().timedTeleport(onlineUser, position.get())
                            .thenAccept(result -> {
                                plugin.getTeleportManager().finishTeleport(onlineUser, result);
                                plugin.performEconomyTransaction(onlineUser, Settings.EconomyAction.RANDOM_TELEPORT);
                            });
                });
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }
}
