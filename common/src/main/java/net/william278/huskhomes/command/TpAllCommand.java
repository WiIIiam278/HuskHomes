package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TpAllCommand extends Command {

    protected TpAllCommand(@NotNull HuskHomes implementor) {
        super("tpall", Permission.COMMAND_TPALL, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length != 0) {
            plugin.getLocales().getLocale("error_invalid_syntax", "/tpaall")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        // Determine players to teleport and teleport them
        plugin.getCache().updatePlayerListCache(plugin, onlineUser).thenAccept(fetchedPlayers -> {
            final List<String> players = fetchedPlayers.stream()
                    .filter(userName -> !userName.equalsIgnoreCase(onlineUser.getUsername())).toList();
            if (players.isEmpty()) {
                plugin.getLocales().getLocale("error_no_players_online").ifPresent(onlineUser::sendMessage);
                return;
            }

            // Send a message
            plugin.getLocales().getLocale("teleporting_all_players", Integer.toString(players.size()))
                    .ifPresent(onlineUser::sendMessage);

            // Teleport every player
            final Position targetPosition = onlineUser.getPosition();
            players.forEach(playerName -> Teleport.builder(plugin, onlineUser)
                    .setTeleporter(playerName)
                    .setTarget(targetPosition)
                    .toTeleport()
                    .thenAccept(Teleport::execute));
        });

    }

}
