package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.manager.RequestsManager;
import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class TeleportRequestCommand extends InGameCommand implements UserListTabProvider {

    private final TeleportRequest.Type requestType;

    protected TeleportRequestCommand(@NotNull HuskHomes plugin, @NotNull TeleportRequest.Type requestType) {
        super(requestType == TeleportRequest.Type.TPA ? "tpa" : "tpahere", List.of(), "<player>", plugin);
        this.requestType = requestType;
    }

    @Override
    public void execute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        final RequestsManager manager = plugin.getManager().requests();
        if (manager.isIgnoringRequests(onlineUser)) {
            plugin.getLocales().getLocale("error_ignoring_teleport_requests")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        final Optional<String> optionalTarget = parseStringArg(args, 0);
        if (optionalTarget.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        final String target = optionalTarget.get();
        if (target.equalsIgnoreCase(onlineUser.getUsername())) {
            plugin.getLocales().getLocale("error_teleport_request_self")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        try {
            manager.sendTeleportRequest(onlineUser, target, requestType);
        } catch (IllegalArgumentException e) {
            plugin.getLocales().getLocale("error_player_not_found", target)
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        plugin.getLocales()
                .getLocale(requestType == TeleportRequest.Type.TPA ? "tpa" : "tpahere" + "_request_sent", target)
                .ifPresent(onlineUser::sendMessage);
    }

}
