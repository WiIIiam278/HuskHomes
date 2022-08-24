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
        super("delwarp", Permission.COMMAND_DELETE_WARP, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length == 1) { //todo delwarp all
            final String warpName = args[0];
            plugin.getSavedPositionManager().deleteWarp(warpName).thenAccept(deleted -> {
                if (deleted) {
                    plugin.getLocales().getLocale("warp_deleted").ifPresent(onlineUser::sendMessage);
                } else {
                    plugin.getLocales().getLocale("error_warp_invalid", warpName).ifPresent(onlineUser::sendMessage);
                }
            });
        } else {
            plugin.getLocales().getLocale("error_invalid_syntax", "/delwarp <name>")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull String[] args, @Nullable OnlineUser user) {
        return args.length > 1 ? Collections.emptyList() : plugin.getCache().warps.stream()
                .filter(s -> s.startsWith(args.length == 1 ? args[0] : ""))
                .sorted().collect(Collectors.toList());
    }
}
