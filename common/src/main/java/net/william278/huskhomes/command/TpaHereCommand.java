package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TpaHereCommand extends Command implements TabProvider {

    protected TpaHereCommand(@NotNull HuskHomes implementor) {
        super("tpahere", Permission.COMMAND_TPAHERE, implementor);
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
                    .sendTeleportRequest(onlineUser, args[0], TeleportRequest.Type.TPA_HERE)
                    .thenAccept(sent -> {
                        if (sent.isEmpty()) {
                            if (args[0].equalsIgnoreCase(onlineUser.getUsername())) {
                                plugin.getLocales().getLocale("error_teleport_request_self")
                                        .ifPresent(onlineUser::sendMessage);
                                return;
                            }

                            plugin.getLocales().getLocale("error_player_not_found", args[0])
                                    .ifPresent(onlineUser::sendMessage);
                            return;
                        }

                        plugin.getLocales().getLocale("tpahere_request_sent", sent.get().getRecipientName())
                                .ifPresent(onlineUser::sendMessage);
                    });
        } else {
            plugin.getLocales().getLocale("error_invalid_syntax", "/tpahere <player>")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    @Override
    @NotNull
    public final List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        return args.length <= 1 ? plugin.getCache().getPlayers().stream()
                .filter(s -> s.toLowerCase().startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                .sorted().collect(Collectors.toList()) : Collections.emptyList();
    }
}
