package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class WarpListCommand extends CommandBase implements ConsoleExecutable {

    protected WarpListCommand(@NotNull HuskHomes implementor) {
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
     * @param onlineUser the user to display warps to
     * @param pageNumber page number to display
     */
    private void showWarpList(@NotNull OnlineUser onlineUser, int pageNumber) {
        if (plugin.getCache().warpLists.containsKey(onlineUser.uuid)) {
            onlineUser.sendMessage(plugin.getCache().warpLists.get(onlineUser.uuid).getNearestValidPage(pageNumber));
            return;
        }

        // Dispatch the warp list event
        plugin.getDatabase().getWarps().thenAccept(warps -> {
            if (warps.isEmpty()) {
                plugin.getLocales().getLocale("error_no_warps_set").ifPresent(onlineUser::sendMessage);
                return;
            }
            plugin.getCache().getWarpList(onlineUser,
                    plugin.getLocales(), warps,
                    plugin.getSettings().permissionRestrictWarps,
                    plugin.getSettings().listItemsPerPage,
                    pageNumber).ifPresent(onlineUser::sendMessage);
        });
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        CompletableFuture.runAsync(() -> {
            final List<Warp> warps = plugin.getDatabase().getWarps().join();
            StringJoiner rowJoiner = new StringJoiner("   ");

            plugin.getLoggingAdapter().log(Level.INFO, "List of " + warps.size() + " warps:");
            for (int i = 1; i <= warps.size(); i++) {
                final String warp = warps.get(i - 1).meta.name;
                rowJoiner.add(warp.length() < 16 ? warp + " ".repeat(16 - warp.length()) : warp);
                if (i % 3 == 0) {
                    plugin.getLoggingAdapter().log(Level.INFO, rowJoiner.toString());
                    rowJoiner = new StringJoiner("   ");
                }
            }
            plugin.getLoggingAdapter().log(Level.INFO, rowJoiner.toString());
        });
    }
}
