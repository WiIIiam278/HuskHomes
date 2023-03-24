package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class BackCommand extends InGameCommand {

    protected BackCommand(@NotNull HuskHomes plugin) {
        super("back", List.of(), "", plugin);
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull String[] args) {
        final Optional<Position> lastPosition = plugin.getDatabase().getLastPosition(executor);
        if (lastPosition.isEmpty()) {
            plugin.getLocales().getLocale("error_no_last_position")
                    .ifPresent(executor::sendMessage);
            return;
        }

        try {
            Teleport.builder(plugin)
                    .teleporter(executor)
                    .target(lastPosition.get())
                    .economyActions(EconomyHook.Action.BACK_COMMAND)
                    .toTimedTeleport()
                    .execute();
        } catch (TeleportationException e) {
            e.displayMessage(executor, plugin, args);
        }
    }

}
