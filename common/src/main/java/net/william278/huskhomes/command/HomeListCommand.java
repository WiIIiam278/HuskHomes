package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class HomeListCommand extends CommandBase implements ConsoleExecutable {

    public HomeListCommand(@NotNull HuskHomes implementor) {
        super("homelist", Permission.COMMAND_HOME, implementor, "homes");
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> showHomeList(onlineUser, onlineUser.username, 1);
            case 1 -> {
                try {
                    int pageNumber = Integer.parseInt(args[0]);
                    showHomeList(onlineUser, onlineUser.username, pageNumber);
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
        if (plugin.getCache().privateHomeLists.containsKey(onlineUser.uuid)) {
            onlineUser.sendMessage(plugin.getCache().privateHomeLists.get(onlineUser.uuid).getNearestValidPage(pageNumber));
            return;
        }
        plugin.getDatabase().getUserDataByName(homeOwner).thenAccept(optionalUser -> optionalUser.ifPresentOrElse(userData -> {
            if (!userData.getUserUuid().equals(onlineUser.uuid)) {
                if (!onlineUser.hasPermission(Permission.COMMAND_HOME_OTHER.node)) {
                    plugin.getLocales().getLocale("error_no_permission").ifPresent(onlineUser::sendMessage);
                    return;
                }
            }
            plugin.getDatabase().getHomes(userData.user()).thenAccept(homes -> {
                if (homes.isEmpty()) {
                    plugin.getLocales().getLocale("error_no_homes_set").ifPresent(onlineUser::sendMessage);
                    return;
                }
                onlineUser.sendMessage(plugin.getCache().getHomeList(onlineUser, userData.user(),
                        plugin.getLocales(), homes, plugin.getSettings().listItemsPerPage, pageNumber));
            });
        }, () -> plugin.getLocales().getLocale("error_invalid_player").ifPresent(onlineUser::sendMessage)));

    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }
}
