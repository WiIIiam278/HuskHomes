package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleportable;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class WarpCommand extends SavedPositionCommand<Warp> {

    protected WarpCommand(@NotNull HuskHomes plugin) {
        super("warp", List.of(), Warp.class, List.of(), plugin);
        setOperatorCommand(true);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (args.length == 0) {
            plugin.getCommand(WarpListCommand.class)
                    .ifPresent(command -> command.showWarpList(executor, 1));
            return;
        }
        super.execute(executor, args);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull Warp warp, @NotNull String[] args) {
        if (plugin.getSettings().isPermissionRestrictWarps()) {
            if (!executor.hasPermission(warp.getPermission()) && !executor.hasPermission(Warp.getWildcardPermission())) {
                plugin.getLocales().getLocale("error_no_permission")
                        .ifPresent(executor::sendMessage);
                return;
            }
        }

        final Optional<Teleportable> optionalTeleporter = resolveTeleportable(executor, args);
        if (optionalTeleporter.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        this.teleport(executor, optionalTeleporter.get(), warp);
    }
}
