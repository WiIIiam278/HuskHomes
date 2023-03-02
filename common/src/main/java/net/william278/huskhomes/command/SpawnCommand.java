package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TimedTeleport;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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

        final Optional<? extends Position> spawn = plugin.getSpawn();
        if (spawn.isEmpty()) {
            plugin.getLocales().getLocale("error_spawn_not_set")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }
        Teleport.builder(plugin, onlineUser)
                .setTarget(spawn.get())
                .toTimedTeleport()
                .thenAccept(TimedTeleport::execute);
    }
}
