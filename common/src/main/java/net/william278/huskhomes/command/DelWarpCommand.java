package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DelWarpCommand extends CommandBase implements TabCompletable {

    protected DelWarpCommand(@NotNull HuskHomes implementor) {
        super("delwarp", Permission.COMMAND_DELETE_WARP, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length == 1) { //todo delwarp all
            final String warpName = args[0];
            plugin.getSavedPositionManager().deleteWarp(warpName).thenAccept(deleted -> {
                if (deleted) {
                    plugin.getLocales().getLocale("warp_deleted", warpName)
                            .ifPresent(onlineUser::sendMessage);
                    return;
                }
                if (warpName.equals("all")) {
                    deleteAllWarps(onlineUser);
                    return;
                }

                plugin.getLocales().getLocale("error_warp_invalid", warpName)
                        .ifPresent(onlineUser::sendMessage);
            });
        } else {
            plugin.getLocales().getLocale("error_invalid_syntax", "/delwarp <name>")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    /**
     * Delete all the server warps
     *
     * @param deleter the player who is deleting the warps
     */
    private void deleteAllWarps(@NotNull OnlineUser deleter) {
        plugin.getDatabase().getWarps().thenAccept(warps -> {
            if (warps.isEmpty()) {
                plugin.getLocales().getLocale("error_no_warps_set")
                        .ifPresent(deleter::sendMessage);
                return;
            }

            final List<CompletableFuture<Boolean>> homeDeletionFuture = new ArrayList<>();
            for (final Warp toDelete : warps) {
                homeDeletionFuture.add(plugin.getSavedPositionManager().deleteWarp(toDelete.meta.name));
            }
            CompletableFuture.allOf(homeDeletionFuture.toArray(new CompletableFuture[0])).thenRun(() ->
                    plugin.getLocales().getLocale("delete_all_warps_success", Integer.toString(homeDeletionFuture.size()))
                            .ifPresent(deleter::sendMessage)).join();
        });
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull String[] args, @Nullable OnlineUser user) {
        return args.length > 1 ? Collections.emptyList() : plugin.getCache().warps.stream()
                .filter(s -> s.startsWith(args.length == 1 ? args[0] : ""))
                .sorted().collect(Collectors.toList());
    }
}
