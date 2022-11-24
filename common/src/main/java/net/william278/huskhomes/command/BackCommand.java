package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TimedTeleport;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class BackCommand extends CommandBase {

    protected BackCommand(@NotNull HuskHomes implementor) {
        super("back", Permission.COMMAND_BACK, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        plugin.getDatabase().getLastPosition(onlineUser).thenAccept(lastPosition ->
                lastPosition.ifPresentOrElse(position -> Teleport.builder(plugin, onlineUser)
                                .setTarget(position)
                                .setEconomyActions(Settings.EconomyAction.BACK_COMMAND)
                                .toTimedTeleport()
                                .thenApply(TimedTeleport::execute),
                        () -> plugin.getLocales().getLocale("error_no_last_position").ifPresent(onlineUser::sendMessage)));
    }

}
