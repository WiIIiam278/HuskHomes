package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DelWarpCommand extends SavedPositionCommand<Warp> {

    public DelWarpCommand(@NotNull HuskHomes implementor) {
        super("delwarp", List.of(), Warp.class, List.of(), implementor);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (handleDeleteAll(executor, args)) {
            return;
        }
        super.execute(executor, args);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull Warp warp, @NotNull String[] args) {
        if (executor instanceof OnlineUser user && !warp.hasPermission(plugin.getSettings().isPermissionRestrictWarps(), user)) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(user::sendMessage);
            return;
        }
        try {
            plugin.getManager().warps().deleteWarp(warp);
        } catch (ValidationException e) {
            e.dispatchWarpError(executor, plugin, warp.getName());
            return;
        }
        plugin.getLocales().getLocale("warp_deleted", warp.getName())
                .ifPresent(executor::sendMessage);
    }

    private boolean handleDeleteAll(@NotNull CommandUser executor, @NotNull String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("all")) {
            if (!parseStringArg(args, 1)
                    .map(confirm -> confirm.equalsIgnoreCase("confirm"))
                    .orElse(false)) {
                plugin.getLocales().getLocale("delete_all_warps_confirm")
                        .ifPresent(executor::sendMessage);
                return true;
            }

            final int deleted = plugin.getManager().warps().deleteAllWarps();
            if (deleted == 0) {
                plugin.getLocales().getLocale("error_no_warps_set")
                        .ifPresent(executor::sendMessage);
                return true;
            }

            plugin.getLocales().getLocale("delete_all_warps_success", Integer.toString(deleted))
                    .ifPresent(executor::sendMessage);
            return true;
        }
        return false;
    }

}