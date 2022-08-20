package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TpOfflineCommand extends CommandBase implements TabCompletable {

    protected TpOfflineCommand(@NotNull HuskHomes implementor) {
        super("tpoffline", Permission.COMMAND_TPOFFLINE, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length != 1) {
            plugin.getLocales().getLocale("error_invalid_syntax", "/tpoffline <player>")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }
        final String targetUser = args[0];
        plugin.getDatabase().getUserDataByName(targetUser).thenAccept(userData -> {
            if (userData.isEmpty()) {
                plugin.getLocales().getLocale("error_player_not_found", targetUser)
                        .ifPresent(onlineUser::sendMessage);
                return;
            }
            plugin.getDatabase().getOfflinePosition(userData.get().user()).thenAccept(offlinePosition -> {
                if (offlinePosition.isEmpty()) {
                    plugin.getLocales().getLocale("error_no_offline_position", targetUser)
                            .ifPresent(onlineUser::sendMessage);
                    return;
                }
                plugin.getTeleportManager().teleport(onlineUser, offlinePosition.get()).thenAccept(
                        result -> plugin.getTeleportManager().finishTeleport(onlineUser, result)).join();
            }).join();
        });
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        return args.length <= 1 ? plugin.getCache().players.stream()
                .filter(s -> s.toLowerCase().startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                .sorted().collect(Collectors.toList()) : Collections.emptyList();
    }
}
