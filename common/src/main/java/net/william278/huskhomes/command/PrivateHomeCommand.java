package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PrivateHomeCommand extends HomeCommand {

    protected PrivateHomeCommand(@NotNull HuskHomes plugin) {
        super("home", List.of(), plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(executor instanceof OnlineUser user)) {
                plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                        .ifPresent(executor::sendMessage);
                return;
            }

            // If the user has a home, teleport them there, otherwise show them their home list
            final List<Home> homes = plugin.getDatabase().getHomes(user);
            if (homes.size() == 1) {
                super.execute(executor, homes.get(0), args);
                return;
            }
            plugin.getCommand(PrivateHomeListCommand.class)
                    .ifPresent(command -> command.showHomeList(executor, user.getUsername(), 1));
            return;
        }
        super.execute(executor, args);
    }

}
