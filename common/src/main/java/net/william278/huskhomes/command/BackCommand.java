package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class BackCommand extends Command {

    protected BackCommand(@NotNull HuskHomes plugin) {
        super("back", List.of(), plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        final OnlineUser player = (OnlineUser) executor;
        plugin.runAsync(() -> {
            final Optional<Position> lastPosition = plugin.getDatabase().getLastPosition(player);
            if (lastPosition.isEmpty()) {
                plugin.getLocales().getLocale("error_no_last_position")
                        .ifPresent(player::sendMessage);
                return;
            }

            Teleport.builder(plugin)
                    .teleporter(player)
                    .target(lastPosition.get())
                    .economyActions(EconomyHook.Action.BACK_COMMAND)
                    .toTimedTeleport()
                    .execute();
        });
    }

}
