package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class HomeListCommand extends Command implements ConsoleExecutable {

    protected HomeListCommand(@NotNull HuskHomes implementor) {
        super("homelist", Permission.COMMAND_HOME, implementor, "homes");
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> showHomeList(onlineUser, onlineUser.getUsername(), 1);
            case 1 -> {
                try {
                    int pageNumber = Integer.parseInt(args[0]);
                    showHomeList(onlineUser, onlineUser.getUsername(), pageNumber);
                } catch (NumberFormatException e) {
                    showHomeList(onlineUser, args[0], 1);
                }
            }
            case 2 -> {
                try {
                    int pageNumber = Integer.parseInt(args[1]);
                    showHomeList(onlineUser, args[0], pageNumber);
                } catch (NumberFormatException e) {
                    plugin.getLocales().getLocale("error_invalid_syntax", "/homelist [player] [page]")
                            .ifPresent(onlineUser::sendMessage);
                }
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/homelist [page]")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    /**
     * Show a (cached) list of a {@link OnlineUser}'s homes
     *
     * @param onlineUser the user to display the homes to
     * @param homeOwner  the user whose homes should be displayed
     * @param pageNumber page number to display
     */
    private void showHomeList(@NotNull OnlineUser onlineUser, @NotNull String homeOwner, int pageNumber) {
        if (plugin.getCache().getPrivateHomeLists().containsKey(homeOwner)) {
            onlineUser.sendMessage(plugin.getCache().getPrivateHomeLists().get(homeOwner).getNearestValidPage(pageNumber));
            return;
        }
        plugin.getDatabase().getUserDataByName(homeOwner).thenAccept(optionalUser -> optionalUser.ifPresentOrElse(userData -> {
            if (!userData.getUserUuid().equals(onlineUser.getUuid())) {
                if (!onlineUser.hasPermission(Permission.COMMAND_HOME_OTHER.node)) {
                    plugin.getLocales().getLocale("error_no_permission").ifPresent(onlineUser::sendMessage);
                    return;
                }
            }
            plugin.getDatabase().getHomes(userData.user()).thenAcceptAsync(homes -> {
                if (homes.isEmpty()) {
                    if (!onlineUser.equals(userData.user())) {
                        plugin.getLocales().getLocale("error_no_homes_set_other",
                                userData.user().username).ifPresent(onlineUser::sendMessage);
                    } else {
                        plugin.getLocales().getLocale("error_no_homes_set").ifPresent(onlineUser::sendMessage);
                    }
                    return;
                }
                plugin.getCache().getHomeList(onlineUser, userData.user(),
                                plugin.getLocales(), homes,
                                plugin.getSettings().getListItemsPerPage(), pageNumber)
                        .ifPresent(onlineUser::sendMessage);
            });
        }, () -> plugin.getLocales().getLocale("error_player_not_found", homeOwner).ifPresent(onlineUser::sendMessage)));

    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        if (args.length != 1) {
            plugin.log(Level.WARNING, "Invalid syntax. Usage: homelist <player>");
            return;
        }
        CompletableFuture.runAsync(() -> {
            final Optional<SavedUser> userData = plugin.getDatabase().getUserDataByName(args[0]).join();
            if (userData.isEmpty()) {
                plugin.log(Level.WARNING, "Player not found: " + args[0]);
                return;
            }
            final List<Home> homes = plugin.getDatabase().getHomes(userData.get().user()).join();
            StringJoiner rowJoiner = new StringJoiner("\t");

            plugin.log(Level.INFO, "List of " + userData.get().user().getUsername() + "'s "
                    + homes.size() + " homes:");
            for (int i = 0; i < homes.size(); i++) {
                final String home = homes.get(i).getMeta().getName();
                rowJoiner.add(home.length() < 16 ? home + " ".repeat(16 - home.length()) : home);
                if ((i + 1) % 3 == 0) {
                    plugin.log(Level.INFO, rowJoiner.toString());
                    rowJoiner = new StringJoiner("\t");
                }
            }
            plugin.log(Level.INFO, rowJoiner.toString());
        });
    }
}
