package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SpawnCommand extends CommandBase {

    protected SpawnCommand(@NotNull HuskHomes implementor) {
        super("spawn", Permission.COMMAND_SPAWN, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length > 0) {
            plugin.getLocales().getLocale("error_invalid_syntax", "/spawn")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        CompletableFuture.runAsync(() -> {
            final Optional<? extends Position> position = (plugin.getSettings().crossServer && plugin.getSettings().globalSpawn
                    ? plugin.getDatabase().getWarp(plugin.getSettings().globalSpawnName).join()
                    : plugin.getServerSpawn().flatMap(spawn -> spawn.getLocation(plugin.getPluginServer())));
            if (position.isEmpty()) {
                plugin.getLocales().getLocale("error_spawn_not_set").ifPresent(onlineUser::sendMessage);
                return;
            }

            plugin.getTeleportManager().timedTeleport(onlineUser, position.get()).join();
        });
    }
}
