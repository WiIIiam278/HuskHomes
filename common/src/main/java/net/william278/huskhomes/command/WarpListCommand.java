package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class WarpListCommand extends Command implements ConsoleExecutable {

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
        if (plugin.getCache().getWarpLists().containsKey(onlineUser.getUuid())) {
            onlineUser.sendMessage(plugin.getCache().getWarpLists().get(onlineUser.getUuid()).getNearestValidPage(pageNumber));
            return;
        }

        // Dispatch the warp list event
        plugin.getDatabase().getWarps()
                .thenApply(warps -> warps.stream()
                        .filter(warp -> warp.hasPermission(plugin.getSettings().isPermissionRestrictWarps(), onlineUser))
                        .collect(Collectors.toList()))
                .thenAccept(warps -> {
                    if (warps.isEmpty()) {
                        plugin.getLocales().getLocale("error_no_warps_set").ifPresent(onlineUser::sendMessage);
                        return;
                    }
                    plugin.getCache().getWarpList(onlineUser, plugin.getLocales(), warps,
                                    plugin.getSettings().getListItemsPerPage(), pageNumber)
                            .ifPresent(onlineUser::sendMessage);
                });
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        plugin.getDatabase().getWarps().thenAccept(warps -> {
            plugin.log(Level.INFO, "List of " + warps.size() + " warps:");

            StringJoiner rowJoiner = new StringJoiner("\t");
            for (int i = 0; i < warps.size(); i++) {
                final String warp = warps.get(i).meta.name;
                rowJoiner.add(warp.length() < 16 ? warp + " ".repeat(16 - warp.length()) : warp);
                if ((i + 1) % 3 == 0) {
                    plugin.log(Level.INFO, rowJoiner.toString());
                    rowJoiner = new StringJoiner("\t");
                }
            }
            plugin.log(Level.INFO, rowJoiner.toString());
        });
    }
}
