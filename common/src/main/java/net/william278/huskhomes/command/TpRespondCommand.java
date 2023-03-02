package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A command used for responding to tp requests - can either be a /tpaccept or /tpdecline command, controlled by the
 * acceptRequestCommand flag
 */
public class TpRespondCommand extends CommandBase implements TabCompletable {

    private final boolean acceptRequestCommand;

    protected TpRespondCommand(@NotNull HuskHomes implementor, boolean acceptRequestCommand) {
        super(acceptRequestCommand ? "tpaccept" : "tpdecline", acceptRequestCommand ? Permission.COMMAND_TPACCEPT : Permission.COMMAND_TPDECLINE, implementor);
        this.acceptRequestCommand = acceptRequestCommand;
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length > 1) {
            plugin.getLocales().getLocale("error_invalid_syntax", "/" + command + " [player]")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        if (args.length == 1) {
            // Respond to the request from specified sender name
            plugin.getRequestManager().respondToTeleportRequestBySenderName(onlineUser, args[0], acceptRequestCommand);
            return;
        }

        plugin.getRequestManager().respondToTeleportRequest(onlineUser, acceptRequestCommand);
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull String[] args, @Nullable OnlineUser user) {
        return args.length <= 1 ? plugin.getCache().getPlayers().stream()
                .filter(s -> s.toLowerCase().startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                .sorted().collect(Collectors.toList()) : Collections.emptyList();
    }
}
