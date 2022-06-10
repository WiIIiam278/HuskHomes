package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class WarpCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    public WarpCommand(@NotNull HuskHomes implementor) {
        super("warp", Permission.COMMAND_WARP, implementor);
    }

    @Override
    public void onExecute(@NotNull Player player, @NotNull String[] args) {
        if (args.length == 1) {
            final String warpName = args[0];
            plugin.getTeleportManager().teleportToWarp(player, warpName);
        } else {
            plugin.getLocales().getLocale("error_invalid_syntax", "/warp [name]")
                    .ifPresent(player::sendMessage);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull Player player, @NotNull String[] args) {
        return plugin.getCache().warps.stream()
                .filter(s -> s.startsWith(args.length >= 1 ? args[0] : ""))
                .sorted().collect(Collectors.toList());
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }
}
