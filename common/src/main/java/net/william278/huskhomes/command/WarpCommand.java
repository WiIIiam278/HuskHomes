package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TimedTeleport;
import net.william278.huskhomes.util.Permission;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class WarpCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    protected WarpCommand(@NotNull HuskHomes implementor) {
        super("warp", Permission.COMMAND_WARP, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> plugin.getDatabase().getWarps().thenAcceptAsync(warps -> {
                if (warps.isEmpty()) {
                    plugin.getLocales().getLocale("error_no_warps_set").ifPresent(onlineUser::sendMessage);
                    return;
                }
                plugin.getCache().getWarpList(onlineUser, plugin.getLocales(), warps,
                                plugin.getSettings().permissionRestrictWarps,
                                plugin.getSettings().listItemsPerPage, 1)
                        .ifPresent(onlineUser::sendMessage);
            });
            case 1 -> {
                final String warpName = args[0];
                plugin.getDatabase()
                        .getWarp(warpName)
                        .thenAccept(warpResult -> warpResult.ifPresentOrElse(warp -> {
                                    // Handle permission restrictions
                                    if (plugin.getSettings().permissionRestrictWarps) {
                                        @Subst("huskhomes.command.warp")
                                        final String warpPermission = Permission.COMMAND_WARP.node + "." + warp.meta.name;

                                        if (!onlineUser.hasPermission(warpPermission) && !onlineUser.hasPermission(Permission.COMMAND_SET_WARP.node)) {
                                            plugin.getLocales().getLocale("error_no_permission")
                                                    .ifPresent(onlineUser::sendMessage);
                                            return;
                                        }
                                    }

                                    Teleport.builder(plugin, onlineUser)
                                            .setTarget(warp)
                                            .toTimedTeleport()
                                            .thenAccept(TimedTeleport::execute);
                                },
                                () -> plugin.getLocales().getLocale("error_warp_invalid", warpName)
                                        .ifPresent(onlineUser::sendMessage)));
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/warp [name]")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull String[] args, @Nullable OnlineUser user) {
        return plugin.getCache().warps.stream()
                .filter(s -> s.toLowerCase().startsWith(args.length >= 1 ? args[0].toLowerCase() : ""))
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        if (args.length != 2) {
            plugin.getLoggingAdapter().log(Level.WARNING, "Invalid syntax. Usage: warp <player> <warp>");
            return;
        }
        final OnlineUser playerToTeleport = plugin.findOnlinePlayer(args[0]).orElse(null);
        if (playerToTeleport == null) {
            plugin.getLoggingAdapter().log(Level.WARNING, "Player not found: " + args[0]);
            return;
        }

        plugin.getDatabase().getWarp(args[1]).thenAccept(optionalWarp -> {
            if (optionalWarp.isEmpty()) {
                plugin.getLoggingAdapter().log(Level.WARNING, "Could not find warp '" + args[1] + "'");
                return;
            }
            final Warp warp = optionalWarp.get();

            plugin.getLoggingAdapter().log(Level.INFO, "Teleporting " + playerToTeleport.username + " to " + warp.meta.name);
            Teleport.builder(plugin, playerToTeleport)
                    .setTarget(warp)
                    .toTimedTeleport()
                    .thenAccept(TimedTeleport::execute);
        });
    }

}
