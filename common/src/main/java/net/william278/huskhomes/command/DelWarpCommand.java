package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DelWarpCommand extends CommandBase implements TabCompletable {

    protected DelWarpCommand(@NotNull HuskHomes implementor) {
        super("delwarp", "<name>", Permission.COMMAND_DELETE_WARP, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length == 0) {
            onlineUser.sendMessage(getSyntaxErrorMessage());
            return;
        }
        if (args.length <= 2) {
            final String warpName = args[0];
            final boolean confirm = args.length == 2 && args[1].equalsIgnoreCase("confirm");
            plugin.getSavedPositionManager().deleteWarp(warpName).thenAccept(deleted -> {
                if (deleted) {
                    plugin.getLocales().getLocale("warp_deleted", warpName)
                            .ifPresent(onlineUser::sendMessage);
                    return;
                }
                if (warpName.equalsIgnoreCase("all")) {
                    deleteAllWarps(onlineUser, confirm);
                    return;
                }

                plugin.getLocales().getLocale("error_warp_invalid", warpName)
                        .ifPresent(onlineUser::sendMessage);
            });
            return;
        }
        onlineUser.sendMessage(getSyntaxErrorMessage());
    }

    /**
     * Delete all the server warps
     *
     * @param deleter the player who is deleting the warps
     * @param confirm whether to skip the confirmation prompt
     */
    private void deleteAllWarps(@NotNull OnlineUser deleter, final boolean confirm) {
        if (!confirm) {
            plugin.getLocales().getLocale("delete_all_warps_confirm")
                    .ifPresent(deleter::sendMessage);
            return;
        }

        plugin.getSavedPositionManager().deleteAllWarps().thenAccept(deleted -> {
            if (deleted == 0) {
                plugin.getLocales().getLocale("error_no_warps_set")
                        .ifPresent(deleter::sendMessage);
                return;
            }

            plugin.getLocales().getLocale("delete_all_warps_success", Integer.toString(deleted))
                    .ifPresent(deleter::sendMessage);
        });
    }

    @Override
    @NotNull
    public List<String> onTabComplete(@NotNull String[] args, @Nullable OnlineUser user) {
        return args.length > 1 ? Collections.emptyList() : plugin.getCache().warps
                .stream()
                .filter(s -> s.startsWith(args.length == 1 ? args[0] : ""))
                .sorted()
                .collect(Collectors.toList());
    }
}
