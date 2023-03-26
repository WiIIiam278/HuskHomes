package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.teleport.Teleportable;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public abstract class HomeCommand extends SavedPositionCommand<Home> {

    protected HomeCommand(@NotNull String name, @NotNull List<String> aliases, @NotNull HuskHomes plugin) {
        super(name, aliases, Home.class, List.of("player"), plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull Home home, @NotNull String[] args) {
        final Optional<Teleportable> optionalTeleporter = resolveTeleporter(executor, args);
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

        this.teleport(executor, optionalTeleporter.get(), home);
    }

}
