package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

public class BackCommand extends CommandBase {

    public BackCommand(@NotNull HuskHomes implementor) {
        super("back", Permission.COMMAND_BACK, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        plugin.getDatabase().getLastPosition(onlineUser).thenAccept(lastPosition ->
                lastPosition.ifPresentOrElse(position -> plugin.getTeleportManager().teleport(onlineUser, position)
                                .thenAccept(result -> plugin.getTeleportManager().finishTeleport(onlineUser, result)),
                        () -> plugin.getLocales().getLocale("error_no_last_position").ifPresent(onlineUser::sendMessage)));
    }

}
