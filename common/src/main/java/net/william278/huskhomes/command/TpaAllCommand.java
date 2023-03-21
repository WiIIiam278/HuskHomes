package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TpaAllCommand extends InGameCommand {

    protected TpaAllCommand(@NotNull HuskHomes plugin) {
        super("tpaall", List.of(), "", plugin);
        setOperatorCommand(true);
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull String[] args) {
        if (plugin.getManager().requests().isIgnoringRequests(executor)) {
            plugin.getLocales().getLocale("error_ignoring_teleport_requests")
                    .ifPresent(executor::sendMessage);
            return;
        }

        plugin.getManager().requests().sendTeleportAllRequest(executor);
        plugin.getLocales().getLocale("tpaall_request_sent")
                .ifPresent(executor::sendMessage);
    }

}
