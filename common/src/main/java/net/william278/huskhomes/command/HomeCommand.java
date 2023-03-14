package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.Teleportable;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public abstract class HomeCommand extends SavedPositionCommand<Home> {

    protected HomeCommand(@NotNull String name, @NotNull List<String> aliases, @NotNull HuskHomes plugin) {
        super(name, aliases, Home.class, List.of(), plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull Home home, @NotNull String[] args) {
        final Optional<Teleportable> optionalTeleporter = parseStringArg(args, 0).map(Teleportable::username)
                .or(() -> executor instanceof Teleportable ? Optional.of((Teleportable) executor) : Optional.empty());
        if (optionalTeleporter.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        if (executor instanceof OnlineUser user && !user.hasPermission(getOtherPermission())
                && (!home.getOwner().equals(user) && !home.isPublic())) {
            plugin.getLocales().getLocale("error_public_home_invalid",
                            home.getOwner().getUsername(), home.getName())
                    .ifPresent(executor::sendMessage);
            return;
        }

        final Teleportable teleporter = optionalTeleporter.get();
        if (!teleporter.equals(executor) && !executor.hasPermission(Permission.COMMAND_TP_OTHER.node)) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        Teleport.builder(plugin)
                .teleporter(teleporter)
                .target(home)
                .toTimedTeleport()
                .execute();
    }

}
