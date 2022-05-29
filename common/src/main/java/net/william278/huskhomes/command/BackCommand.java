package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.player.User;
import org.jetbrains.annotations.NotNull;

public class BackCommand extends CommandBase {

    private final static String PERMISSION = "huskhomes.command.back";

    public BackCommand(@NotNull HuskHomes implementor) {
        super("back", PERMISSION, implementor);
    }

    @Override
    public void onExecute(@NotNull Player player, @NotNull String[] args) {
        plugin.getDatabase().getLastPosition(new User(player)).thenAccept(lastPosition ->
                lastPosition.ifPresentOrElse(position -> plugin.getTeleportManager().teleport(player, position)
                                .thenAccept(result -> plugin.getTeleportManager().finishTeleport(player, result)),
                        () -> plugin.getLocales().getLocale("error_no_last_position").ifPresent(player::sendMessage)));
    }

}
