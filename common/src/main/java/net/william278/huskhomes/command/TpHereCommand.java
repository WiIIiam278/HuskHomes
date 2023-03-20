package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.teleport.Target;
import net.william278.huskhomes.teleport.Teleportable;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TpHereCommand extends InGameCommand implements UserListTabProvider {

    protected TpHereCommand(@NotNull HuskHomes plugin) {
        super("tphere", List.of("tpohere"), "<player>", plugin);
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
