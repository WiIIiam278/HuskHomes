package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.request.TeleportRequest;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TpaHereCommand extends CommandBase implements TabCompletable {

    protected TpaHereCommand(@NotNull HuskHomes implementor) {
        super("tpahere", Permission.COMMAND_TPAHERE, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length == 1) {
            plugin.getRequestManager().sendTeleportRequest(onlineUser, args[0], TeleportRequest.RequestType.TPA_HERE).thenAccept(sent -> {
                if (!sent) {
                    if (plugin.findPlayer(args[0]).isPresent()) {
                        plugin.getLocales().getLocale("error_teleport_request_self")
                                .ifPresent(onlineUser::sendMessage);
                        return;
                    }
                    plugin.getLocales().getLocale("error_player_not_found", args[0])
                            .ifPresent(onlineUser::sendMessage);
                    return;
                }
                plugin.getLocales().getLocale("tpahere_request_sent", args[0])
                        .ifPresent(onlineUser::sendMessage);
            });
        } else {
            plugin.getLocales().getLocale("error_invalid_syntax", "/tpahere <player>")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull String[] args, @Nullable OnlineUser user) {
        return args.length <= 1 ? plugin.getCache().players.stream()
                .filter(s -> s.toLowerCase().startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                .sorted().collect(Collectors.toList()) : Collections.emptyList();
    }
}
