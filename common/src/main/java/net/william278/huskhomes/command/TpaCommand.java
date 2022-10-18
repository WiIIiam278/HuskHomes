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

public class TpaCommand extends CommandBase implements TabCompletable {

    protected TpaCommand(@NotNull HuskHomes implementor) {
        super("tpa", Permission.COMMAND_TPA, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (plugin.getRequestManager().isIgnoringRequests(onlineUser)) {
            plugin.getLocales().getLocale("error_ignoring_teleport_requests")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        if (args.length == 1) {
            plugin.getRequestManager()
                    .sendTeleportRequest(onlineUser, args[0], TeleportRequest.RequestType.TPA)
                    .thenAccept(sent -> {
                        if (sent.isEmpty()) {
                            if (args[0].equalsIgnoreCase(onlineUser.username)) {
                                plugin.getLocales().getLocale("error_teleport_request_self")
                                        .ifPresent(onlineUser::sendMessage);
                                return;
                            }

                            plugin.getLocales().getLocale("error_player_not_found", args[0])
                                    .ifPresent(onlineUser::sendMessage);
                            return;
                        }

                        plugin.getLocales().getLocale("tpa_request_sent", sent.get().getRecipientName())
                                .ifPresent(onlineUser::sendMessage);
                    });
        } else {
            plugin.getLocales().getLocale("error_invalid_syntax", "/tpa <player>")
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
