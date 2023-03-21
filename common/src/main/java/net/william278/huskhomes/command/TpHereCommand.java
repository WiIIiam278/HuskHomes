package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class TpHereCommand extends InGameCommand implements UserListTabProvider {

    protected TpHereCommand(@NotNull HuskHomes plugin) {
        super("tphere", List.of("tpohere"), "<player>", plugin);
        setOperatorCommand(true);
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull String[] args) {
        final Optional<String> optionalTarget = parseStringArg(args, 0);
        if (optionalTarget.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        try {
            Teleport.builder(plugin)
                    .executor(executor)
                    .teleporter(optionalTarget.get())
                    .target(executor.getPosition())
                    .toTeleport().execute();
        } catch (TeleportationException e) {
            e.displayMessage(executor, plugin, args);
        }
    }

}
