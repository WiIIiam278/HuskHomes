package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TpOfflineCommand extends Command implements TabProvider {

    protected TpOfflineCommand(@NotNull HuskHomes plugin) {
        super("tpoffline", List.of(), "<player>", plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        if (!(executor instanceof OnlineUser user)) {
            plugin.getLocales().getLocale("error_in_game_only")
                    .ifPresent(executor::sendMessage);
            return;
        }

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

        this.teleportToOfflinePosition(user, targetUserData.get(), args);
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

            plugin.getLocales().getLocale("teleporting_offline_complete", target.getUsername())
                    .ifPresent(user::sendMessage);
        } catch (TeleportationException e) {
            e.displayMessage(user, plugin, args);
        }
    }

    @Override
    @NotNull
    public final List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        return args.length <= 1 ? plugin.getCache().getPlayers().stream()
                .filter(s -> s.toLowerCase().startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                .sorted().collect(Collectors.toList()) : Collections.emptyList();
    }

}
