package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * A command used for responding to tp requests - can either be a /tpaccept or /tpdecline command, controlled by the
 * acceptRequestCommand flag
 */
public class TpRespondCommand extends InGameCommand implements UserListTabProvider {

    private final boolean accept;

    protected TpRespondCommand(@NotNull HuskHomes plugin, boolean accept) {
        super(accept ? "tpaccept" : "tpdecline", List.of(), "[player]", plugin);
        this.accept = accept;
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull String[] args) {
        final Optional<String> requesterName = parseStringArg(args, 0);
        if (requesterName.isPresent()) {
            plugin.getManager().requests().respondToTeleportRequestBySenderName(executor, requesterName.get(), accept);
            return;
        }
        plugin.getManager().requests().respondToTeleportRequest(executor, accept);
    }

}
