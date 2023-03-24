package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class TpOfflineCommand extends InGameCommand implements UserListTabProvider {

    protected TpOfflineCommand(@NotNull HuskHomes plugin) {
        super("tpoffline", List.of(), "<player>", plugin);
        setOperatorCommand(true);
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull String[] args) {
        final Optional<String> optionalUser = parseStringArg(args, 0);
        if (optionalUser.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        final Optional<User> targetUserData = plugin.getDatabase()
                .getUserDataByName(optionalUser.get())
                .map(SavedUser::getUser);
        if (targetUserData.isEmpty()) {
            plugin.getLocales().getLocale("error_player_not_found", optionalUser.get())
                    .ifPresent(executor::sendMessage);
            return;
        }

        this.teleportToOfflinePosition(executor, targetUserData.get(), args);
    }

    private void teleportToOfflinePosition(@NotNull OnlineUser user, @NotNull User target, @NotNull String[] args) {
        final Optional<Position> position = plugin.getDatabase().getOfflinePosition(target);
        if (position.isEmpty()) {
            plugin.getLocales().getLocale("error_no_offline_position", target.getUsername())
                    .ifPresent(user::sendMessage);
            return;
        }

        plugin.getLocales().getLocale("teleporting_offline_player", target.getUsername())
                .ifPresent(user::sendMessage);
        try {
            Teleport.builder(plugin)
                    .teleporter(user)
                    .target(position.get())
                    .toTeleport().execute();
        } catch (TeleportationException e) {
            e.displayMessage(user, plugin, args);
        }
    }

}
