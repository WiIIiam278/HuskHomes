package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class HomeListCommand extends Command {

    protected HomeListCommand(@NotNull HuskHomes plugin) {
        super("homelist", List.of("homes"), "[player] [page]", plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        final Optional<String> homeOwner = args.length == 2 ? parseStringArg(args, 0)
                : executor instanceof OnlineUser user ? Optional.of(user.getUsername()) : Optional.empty();
        final int pageNumber = parseIntArg(args, args.length > 0 ? args.length - 1 : 0).orElse(1);
        if (homeOwner.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        showHomeList(executor, homeOwner.get(), pageNumber);
    }

    private void showHomeList(@NotNull CommandUser executor, @NotNull String homeOwner, int pageNumber) {
        if (plugin.getCache().getPrivateHomeLists().containsKey(homeOwner)) {
            executor.sendMessage(plugin.getCache().getPrivateHomeLists().get(homeOwner).getNearestValidPage(pageNumber));
            return;
        }

        final Optional<User> targetUser = plugin.getDatabase().getUserDataByName(homeOwner).map(SavedUser::getUser);
        if (targetUser.isEmpty()) {
            plugin.getLocales().getLocale("error_player_not_found", homeOwner)
                    .ifPresent(executor::sendMessage);
            return;
        }

        final User user = targetUser.get();
        if (executor instanceof OnlineUser onlineUser && !user.getUuid().equals(onlineUser.getUuid())) {
            if (!executor.hasPermission(getPermission("other"))) {
                plugin.getLocales().getLocale("error_no_permission").ifPresent(executor::sendMessage);
                return;
            }
        }

        final List<Home> homes = plugin.getDatabase().getHomes(user);
        if (homes.isEmpty()) {
            if (!executor.equals(user)) {
                plugin.getLocales().getLocale("error_no_homes_set_other", user.getUsername())
                        .ifPresent(executor::sendMessage);
            } else {
                plugin.getLocales().getLocale("error_no_homes_set").ifPresent(executor::sendMessage);
            }
            return;
        }
        plugin.getCache().getHomeList(executor, user,
                        plugin.getLocales(), homes,
                        plugin.getSettings().getListItemsPerPage(), pageNumber)
                .ifPresent(executor::sendMessage);
    }

}
