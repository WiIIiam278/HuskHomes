package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.list.PublicHomeList;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class PublicHomeListCommand extends CommandBase implements ConsoleExecutable {

    public PublicHomeListCommand(@NotNull HuskHomes implementor) {
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
        if (plugin.getCache().positionLists.containsKey(onlineUser.uuid)) {
            if (plugin.getCache().positionLists.get(onlineUser.uuid) instanceof PublicHomeList publicHomeList) {
                publicHomeList.getDisplay(pageNumber).forEach(onlineUser::sendMessage);
                return;
            }
        }

        plugin.getDatabase().getPublicHomes().thenAccept(warps -> {
            if (warps.isEmpty()) {
                plugin.getLocales().getLocale("error_no_public_homes_set").ifPresent(onlineUser::sendMessage);
                return;
            }
            final PublicHomeList warpList = new PublicHomeList(warps, plugin);
            plugin.getCache().positionLists.put(onlineUser.uuid, warpList);
            warpList.getDisplay(pageNumber).forEach(onlineUser::sendMessage);
        });

    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }
}
