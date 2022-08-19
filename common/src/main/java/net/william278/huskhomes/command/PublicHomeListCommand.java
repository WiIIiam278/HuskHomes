package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class PublicHomeListCommand extends CommandBase implements ConsoleExecutable {

    protected PublicHomeListCommand(@NotNull HuskHomes implementor) {
        super("publichomelist", Permission.COMMAND_HOME, implementor, "phomelist", "phomes");
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> showPublicHomeList(onlineUser, 1);
            case 1 -> {
                try {
                    int pageNumber = Integer.parseInt(args[0]);
                    showPublicHomeList(onlineUser, pageNumber);
                } catch (NumberFormatException e) {
                    plugin.getLocales().getLocale("error_invalid_syntax", "/publichomelist [page]")
                            .ifPresent(onlineUser::sendMessage);
                }
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/publichomelist [page]")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    /**
     * Show a (cached) list of public homes
     *
     * @param onlineUser the user to display the homes to
     * @param pageNumber page number to display
     */
    private void showPublicHomeList(@NotNull OnlineUser onlineUser, int pageNumber) {
        if (plugin.getCache().publicHomeLists.containsKey(onlineUser.uuid)) {
            onlineUser.sendMessage(plugin.getCache().publicHomeLists.get(onlineUser.uuid).getNearestValidPage(pageNumber));
            return;
        }

        plugin.getDatabase().getPublicHomes().thenAccept(publicHomes -> {
            if (publicHomes.isEmpty()) {
                plugin.getLocales().getLocale("error_no_public_homes_set").ifPresent(onlineUser::sendMessage);
                return;
            }
            onlineUser.sendMessage(plugin.getCache().getPublicHomeList(onlineUser, plugin.getLocales(), publicHomes,
                    plugin.getSettings().listItemsPerPage, pageNumber));
        });

    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }
}
