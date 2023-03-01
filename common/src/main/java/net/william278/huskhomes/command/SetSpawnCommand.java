package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class SetSpawnCommand extends CommandBase {

    protected SetSpawnCommand(@NotNull HuskHomes implementor) {
        super("setspawn", Permission.COMMAND_SET_SPAWN, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length > 0) {
            plugin.getLocales().getLocale("error_invalid_syntax", "/setspawn")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        final Position position = onlineUser.getPosition();
        if (plugin.getSettings().isCrossServer() && plugin.getSettings().isGlobalSpawn()) {
            plugin.getDatabase().getWarp(plugin.getSettings().getGlobalSpawnName()).thenApply(warp -> {
                if (warp.isPresent()) {
                    return plugin.getManager().updateWarpPosition(warp.get(), position);
                } else {
                    return plugin.getManager().setWarp(new PositionMeta(plugin.getSettings().getGlobalSpawnName(),
                                    plugin.getLocales().getRawLocale("spawn_warp_default_description").orElse("")), position)
                            .thenApply(result -> result.resultType().successful);
                }
            }).thenAccept(result -> result.thenAccept(successful -> {
                if (successful) {
                    plugin.getLocales().getLocale("set_spawn_success")
                            .ifPresent(onlineUser::sendMessage);
                }
            }));
        } else {
            plugin.setServerSpawn(position);
            plugin.getLocales().getLocale("set_spawn_success")
                    .ifPresent(onlineUser::sendMessage);
        }
    }
}
