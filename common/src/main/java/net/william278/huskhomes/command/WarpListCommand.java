package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.list.WarpList;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class WarpListCommand extends CommandBase implements ConsoleExecutable {

    public WarpListCommand(@NotNull HuskHomes implementor) {
        super("warplist", Permission.COMMAND_WARP, implementor);
    }

    @Override
    public void onExecute(@NotNull Player player, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> showWarpList(player, 1);
            case 1 -> {
                try {
                    int pageNumber = Integer.parseInt(args[0]);
                    showWarpList(player, pageNumber);
                } catch (NumberFormatException e) {
                    plugin.getLocales().getLocale("error_invalid_syntax", "/warplist [page]")
                            .ifPresent(player::sendMessage);
                }
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/warplist [page]")
                    .ifPresent(player::sendMessage);
        }
    }

    /**
     * Show a (cached) list of server warps
     *
     * @param player     the user to display warps to
     * @param pageNumber page number to display
     */
    private void showWarpList(@NotNull Player player, int pageNumber) {
        if (plugin.getCache().positionLists.containsKey(player.getUuid())) {
            if (plugin.getCache().positionLists.get(player.getUuid()) instanceof WarpList warpList) {
                warpList.getDisplay(pageNumber).forEach(player::sendMessage);
                return;
            }
        }
        final User user = new User(player);
        plugin.getDatabase().getWarps().thenAccept(warps -> {
            if (warps.isEmpty()) {
                plugin.getLocales().getLocale("error_no_warps_set").ifPresent(player::sendMessage);
                return;
            }
            final WarpList warpList = new WarpList(warps, plugin);
            plugin.getCache().positionLists.put(user.uuid, warpList);
            warpList.getDisplay(pageNumber).forEach(player::sendMessage);
        });
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }
}
