package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.util.Permission;
import net.william278.huskhomes.util.RegexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class DelWarpCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    public DelWarpCommand(@NotNull HuskHomes implementor) {
        super("delwarp", Permission.COMMAND_DELETE_WARP, implementor);
    }

    @Override
    public void onExecute(@NotNull Player player, @NotNull String[] args) {
        if (args.length == 1) {
            plugin.getSavedPositionManager().deleteWarp(args[0]);
        } else {
            plugin.getLocales().getLocale("error_invalid_syntax", "/delwarp <name>")
                    .ifPresent(player::sendMessage);
        }
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {

    }

    @Override
    public List<String> onTabComplete(@NotNull Player player, @NotNull String[] args) {
        return plugin.getCache().warps.stream()
                .filter(s -> s.startsWith(args.length >= 1 ? args[0] : ""))
                .sorted().collect(Collectors.toList());
    }
}
