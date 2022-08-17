package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class WarpListCommand extends CommandBase implements ConsoleExecutable {

    public WarpListCommand(@NotNull HuskHomes implementor) {
        super("warplist", Permission.COMMAND_WARP, implementor, "warps");
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> showWarpList(onlineUser, 1);
            case 1 -> {
                try {
                    int pageNumber = Integer.parseInt(args[0]);
                    showWarpList(onlineUser, pageNumber);
                } catch (NumberFormatException e) {
                    plugin.getLocales().getLocale("error_invalid_syntax", "/warplist [page]")
                            .ifPresent(onlineUser::sendMessage);
                }
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/warplist [page]")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    /**
     * Show a (cached) list of server warps
     *
     * @param onlineUser     the user to display warps to
     * @param pageNumber page number to display
     */
    private void showWarpList(@NotNull OnlineUser onlineUser, int pageNumber) {
        if (plugin.getCache().warpLists.containsKey(onlineUser.uuid)) {
            onlineUser.sendMessage(plugin.getCache().warpLists.get(onlineUser.uuid).getNearestValidPage(pageNumber));
            return;
        }
        
        plugin.getDatabase().getWarps().thenAccept(warps -> {
            if (warps.isEmpty()) {
                plugin.getLocales().getLocale("error_no_warps_set").ifPresent(onlineUser::sendMessage);
                return;
            }
            onlineUser.sendMessage(plugin.getCache().getWarpList(onlineUser, plugin.getLocales(), warps,
                    plugin.getSettings().listItemsPerPage, pageNumber));
        });
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }
}
