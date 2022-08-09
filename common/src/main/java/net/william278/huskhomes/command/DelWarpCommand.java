package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class DelWarpCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    public DelWarpCommand(@NotNull HuskHomes implementor) {
        super("delwarp", Permission.COMMAND_DELETE_WARP, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length == 1) {
            plugin.getSavedPositionManager().deleteWarp(args[0]);
        } else {
            plugin.getLocales().getLocale("error_invalid_syntax", "/delwarp <name>")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {

    }

    @Override
    public List<String> onTabComplete(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        return plugin.getCache().warps.stream()
                .filter(s -> s.startsWith(args.length >= 1 ? args[0] : ""))
                .sorted().collect(Collectors.toList());
    }
}
